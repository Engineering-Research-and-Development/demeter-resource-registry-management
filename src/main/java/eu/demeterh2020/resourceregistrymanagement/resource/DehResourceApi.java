package eu.demeterh2020.resourceregistrymanagement.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.Attachment;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.DehResourceForCreationDTO;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.DehResourceForCreationDtoMultipart;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.JsonResponse;
import eu.demeterh2020.resourceregistrymanagement.exception.BadRequestException;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceAlreadyExists;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceNotFoundException;
import eu.demeterh2020.resourceregistrymanagement.service.AttachmentService;
import eu.demeterh2020.resourceregistrymanagement.service.AuditService;
import eu.demeterh2020.resourceregistrymanagement.service.DehResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import net.minidev.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "api/v1/resources", produces = {MediaType.APPLICATION_JSON_VALUE})
public class DehResourceApi {

    private final static Logger log = LoggerFactory.getLogger(DehResourceApi.class);

    @Autowired
    private DehResourceService dehResourceService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private ModelMapper modelMapper;

    @Operation(summary = "Register new DEH Resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource registered",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DehResource.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public JsonResponse saveDehResource(@Valid @RequestBody DehResourceForCreationDTO dehResourceForCreationDTO) {

        log.info("saveDehResource() called.");

        DehResource converted = convertToDehResource(dehResourceForCreationDTO);
        DehResource savedResource = dehResourceService.save(converted);

        log.info("DEH Resource saved with uid:" + savedResource.getUid());

        return new JsonResponse(true, "DEH Resource successfully created", savedResource, null);
    }


    @Operation(summary = "Register new DEH Resource Test Multipart form")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource registered",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DehResource.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @PostMapping(path = "/multipart", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public JsonResponse saveDehResourceMultipartForm(@ModelAttribute @Valid DehResourceForCreationDtoMultipart dehResourceDto,
                                                     BindingResult result) throws IOException {

        log.info("saveDehResource() called.");

        String validationErrors = checkValidationError(result);

        if (validationErrors == null) {
            //Check if resource with same name exists
            if (dehResourceService.existByName(dehResourceDto.getName())) {
                throw new ResourceAlreadyExists("Resource with a name " + dehResourceDto.getName() + " already exists");
            }

            if (dehResourceDto.getAttachmentFile() != null
                    && !(dehResourceDto.getAttachmentFile().iterator().next().getResource().getFilename().equalsIgnoreCase(""))) {

                List<Attachment> savedAttachments = attachmentService.saveMultipleAttachments(dehResourceDto.getAttachmentFile());
                DehResource converted = convertFromMultipartToDehResource(dehResourceDto, savedAttachments);

                DehResource savedResource = dehResourceService.save(converted);

                log.info("DEH Resource saved with uid:" + savedResource.getUid());

                return new JsonResponse(true, "DEH Resource successfully created", savedResource, null);
            }

            DehResource converted = convertFromMultipartToDehResource(dehResourceDto, null);

            DehResource savedResource = dehResourceService.save(converted);

            log.info("DEH Resource saved with uid:" + savedResource.getUid());

            return new JsonResponse(true, "DEH Resource successfully created", savedResource, null);
        }

        throw new BadRequestException(validationErrors);
    }

    @Operation(summary = "Delete existing DEH Resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource deleted",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DEH Resource not found",
                    content = @Content)})
    @DeleteMapping(value = "/{uid}")
    public JsonResponse deleteDehResource(@PathVariable("uid") String uid) {

        log.info("deleteDehResource() called.");

        Optional<DehResource> dehResource = dehResourceService.findOneByUid(uid);
        if (dehResource.isPresent()) { // resource exist in DB
            dehResourceService.deleteByUid(uid);
            return new JsonResponse(true, "DEH Resource successfully deleted", null, "Successfully deleted resource with uid: " + uid);
        }

        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
    }

    @Operation(hidden = true, summary = "Partial update existing DEH Resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource updated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DehResource.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DEH Resource not found",
                    content = @Content)})
    @PatchMapping(path = "/{uid}", consumes = {"application/json-patch+json"})
    public JsonResponse partialUpdateDehResource(@PathVariable(value = "uid") String uid, @RequestBody JsonPatch patch) throws JsonPatchException, JsonProcessingException {

        log.info("partialUpdateDehResource() called.");

        DehResource dehResourcePatched = dehResourceService.partialUpdate(uid, patch);
        auditService.update(dehResourcePatched);

        log.info("DEH Resource with uid:" + uid + " patched.", dehResourcePatched);

        return new JsonResponse(true, "DEH Resource successfully updated", dehResourcePatched, null);

    }

    @Operation(summary = "Update existing DEH Resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource updated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DEH Resource not found",
                    content = @Content)})
    @PutMapping(path = "/{uid}", consumes = {"application/json"})
    public JsonResponse updateDehResource(@PathVariable(value = "uid") String uid,
                                          @Valid @RequestBody DehResourceForCreationDTO dehResourceForUpdating) {

        log.info("updateDehResource() called.");

        DehResource updatedDehResource = dehResourceService.update(uid, dehResourceForUpdating);
        auditService.update(updatedDehResource);

        log.info("DEH Resource with uid:" + uid + " updated.", updatedDehResource);

        return new JsonResponse(true, "DEH Resource successfully updated", updatedDehResource, null);
    }

    //TODO check with Marco and change the updating of attachment
    @PutMapping(path = "multipart/{uid}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public JsonResponse updateDehResourceMultipartForm(@PathVariable(value = "uid") String uid,
                                                       @ModelAttribute @Valid DehResourceForCreationDtoMultipart dehResourceDto,
                                                       BindingResult result) throws IOException {

        log.info("updateDehResourceMultipartForm() called.");

        String validationErrors = checkValidationError(result);

        if (validationErrors == null) {

            DehResource updatedDehResource = dehResourceService.updateMultipartForm(uid, dehResourceDto);
            auditService.update(updatedDehResource);

            log.info("DEH Resource with uid:" + uid + " updated.", updatedDehResource);

            return new JsonResponse(true, "DEH Resource successfully updated", updatedDehResource, null);

        }

        throw new BadRequestException(validationErrors);
    }

    @Operation(summary = "Find DEH Resource by uid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DehResource.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DEH Resource not found",
                    content = @Content)})
    @GetMapping(value = "/{uid}")
    public JsonResponse findOneByUid(@PathVariable String uid) {

        log.info("findOneByUid() called.");

        Optional<DehResource> dehResource = dehResourceService.findOneByUid(uid);

        //TODO change implementation regarding DYMER
        if (dehResource.isPresent()) { // resource exist in DB
            DehResource numberOfDownloads = dehResourceService.updateNumberOfDownloads(uid);
            return new JsonResponse(true, "DEH Resource found", numberOfDownloads, null);
        }
        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
    }

    @Operation(summary = "List all DEH Resources")
    @GetMapping
    public JsonResponse findAll(@RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                                @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize,
                                @RequestParam(name = "sortBy", required = false, defaultValue = "name") String sortBy,
                                @RequestParam(name = "sortingOrder", required = false, defaultValue = "ASC") Sort.Direction sortingOrder) {

        log.info("findAll() called.");

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortingOrder, sortBy);
        Page<DehResource> allResources = dehResourceService.findAll(pageable);

        return new JsonResponse(true, "All DEH Resources found", allResources.getContent(), createPagingExtraData(allResources));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource Categories",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DEH Resource Categories not found",
                    content = @Content)})
    @Operation(hidden = true, summary = "List all names of DEH Resources Categories")
    @GetMapping(value = "/categories")
    public JsonResponse findAllCategories() {

        log.info("findAllCategories() called.");

        List<String> allCategories = dehResourceService.findAllCategories();

        return new JsonResponse(true, "All categories found", allCategories, null);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource Types",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DEH Resource Types not found",
                    content = @Content)})
    @Operation(hidden = true, summary = "List all names of DEH Resources Types")
    @GetMapping(value = "/types")
    public JsonResponse findAllTypes() {

        log.info("findAllTypes() called.");

        List<String> allTypes = dehResourceService.findAllTypes();

        return new JsonResponse(true, "All types found", allTypes, null);
    }

    @Operation(summary = "Search for a DEH Resource by Filters")
    @GetMapping(value = "/search")
    public JsonResponse search(@RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                               @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize,
                               @RequestParam(name = "sortBy", required = false, defaultValue = "name") String sortBy,
                               @RequestParam(name = "sortingOrder", required = false, defaultValue = "ASC") Sort.Direction sortingOrder,
                               @RequestParam(name = "localisationDistance", required = false) String localisationDistance,
                               @RequestParam(name = "uid", required = false) String resourceUid,
                               @QuerydslPredicate(root = DehResource.class) Predicate predicate) {

        log.info("search() called.");

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortingOrder, sortBy);

        if (localisationDistance != null) {
            log.info("Filtered search with distance");

            Page<DehResource> allResources = dehResourceService.findAllByQuery(predicate, pageable, localisationDistance);

            return new JsonResponse(true, "All DEH Resources by search criteria found", allResources.getContent(), createPagingExtraData(allResources));

        }
        if (resourceUid != null) {
            Optional<DehResource> resource = dehResourceService.findOneByUid(resourceUid);
            return new JsonResponse(true, "Resource by uid found", resource, null);
        }

        Page<DehResource> allResources = dehResourceService.findAllByQuery(predicate, pageable, null);

        return new JsonResponse(true, "All DEH Resources by search criteria found", allResources.getContent(), createPagingExtraData(allResources));
    }

    @Operation(summary = "Rate DEH Resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource rated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DehResource.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DEH Resource not found",
                    content = @Content)})
    @PostMapping(value = "/{uid}/rate")
    @CrossOrigin
    public JsonResponse rateResource(@PathVariable String uid, @RequestBody Double rating) {

        log.info("rateResource() called.");

        DehResource ratedDehResource = dehResourceService.rateResource(uid, rating);

        return new JsonResponse(true, "DEH Resource successfully updated", ratedDehResource, null);
    }


    /**
     * Private method for converting DEHResourceForCreation to DehResource POJO class
     */
    private DehResource convertToDehResource(DehResourceForCreationDTO dehResourceForCreationDTO) {
        // Mapping DehResourceForCreationDTO to DehResource
        DehResource dehResource = modelMapper.map(dehResourceForCreationDTO, DehResource.class);

        return dehResource;
    }

    /**
     * Private method for converting DEHResourceForCreation to DehResource POJO class
     */
    private DehResource convertFromMultipartToDehResource(DehResourceForCreationDtoMultipart dehResourceForCreationDTO, List<Attachment> savedAttachments) {
        // Mapping DehResourceForCreationDTO to DehResource
        GeoJsonPoint location = new GeoJsonPoint(dehResourceForCreationDTO.getLocalisation().getX(), dehResourceForCreationDTO.getLocalisation().getY());
        DehResource dehResource = modelMapper.map(dehResourceForCreationDTO, DehResource.class);
        dehResource.getLocalisation().add(location);
        dehResource.setAttachment(savedAttachments);
        return dehResource;
    }


    /**
     * Private method for checking value types in DEHResourceForCreation object
     */
    private String checkValueType(String value) {
        String message = new String();
        switch (value) {
            case "status":
                message = ", correct type is: int.  Available values: 1 - Published, 2 - Not published, 3 - Draft.";
                break;
            case "attachmentFile":
                message = ", correct type is: MultipartFile.";
                break;
            case "maturityLevel":
                message = ", correct type is: int.";
                break;
            case "accessibility":
                message = ", correct type is: int.  Available values: 0 - Public, 1 - Private, 2 - Restricted.";
                break;
            case "localisation":
                message = ", correct type is pair of coordinates (x,y). Example: 12,321321, 14, 512321.";
                break;
            case "accessibilityConstraint":
                message = ". Available values: 0 - Public, 1 - Private, 2 - Restricted.";
                break;
            case "statusConstraint":
                message = ". Available values: 1 - Published, 2 - Not published, 3 - Draft.";

        }
        return message;
    }

    /**
     * Private method for checking validation errors in DEHResourceForCreation
     */
    private String checkValidationError(BindingResult result) {

        List<String> errors = new ArrayList<>();

        //Check if there are errors in validation
        if (result.hasErrors()) {
            errors.add("Please correct next values:");
            result.getFieldErrors().iterator().forEachRemaining(fieldError -> {
                if (!fieldError.getCode().equalsIgnoreCase("MAX") || fieldError.getCode().equalsIgnoreCase("MIN")) {
                    errors.add(fieldError.getField() + " can't be type: " + fieldError.getRejectedValue().getClass().getSimpleName() + checkValueType(fieldError.getField()));
                }
                if (fieldError.getCode().equalsIgnoreCase("MAX")) {
                    errors.add(fieldError.getField() + " can't be: " + fieldError.getRejectedValue() + checkValueType(fieldError.getField().concat("Constraint")));
                }
                if (fieldError.getCode().equalsIgnoreCase("MIN")) {
                    errors.add(fieldError.getField() + " can't be: " + fieldError.getRejectedValue() + checkValueType(fieldError.getField().concat("Constraint")));
                }
            });

            return errors.stream().collect(Collectors.joining(" "));
        }

        return null;
    }

    /**
     * Private method for creating extra data related to paging
     */
    private JSONObject createPagingExtraData(Page<DehResource> page) {

        JSONObject pageable = new JSONObject();

        if (page.getPageable().toOptional().isPresent()) {
            JSONObject pageableInfo = new JSONObject();

            pageableInfo.appendField("sort", page.getPageable().getSort());
            pageableInfo.appendField("pageNumber", page.getPageable().getPageNumber());
            pageableInfo.appendField("pageSize", page.getPageable().getPageSize());
            pageableInfo.appendField("offset", page.getPageable().getOffset());
            pageableInfo.appendField("unpaged", page.getPageable().isUnpaged());
            pageableInfo.appendField("paged", page.getPageable().isPaged());
            pageableInfo.appendField("totalPages", page.getTotalPages());
            pageableInfo.appendField("totalElements", page.getTotalElements());
            pageableInfo.appendField("last", page.isLast());
            pageableInfo.appendField("first", page.isFirst());
            pageableInfo.appendField("numberOfElements", page.getNumberOfElements());


            pageable.appendField("pageable", pageableInfo);
        } else {
            pageable.appendField("pageable", "Bad paging request, all resources returned");
        }

        return pageable;
    }
}
