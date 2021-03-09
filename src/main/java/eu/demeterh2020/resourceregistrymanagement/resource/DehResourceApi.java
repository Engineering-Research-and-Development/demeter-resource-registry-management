package eu.demeterh2020.resourceregistrymanagement.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.Attachment;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.DehResourceForCreationDTO;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.DehResourceForCreationDtoMultipart;
import eu.demeterh2020.resourceregistrymanagement.exception.BadRequestException;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceAlreadyExists;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceNotFoundException;
import eu.demeterh2020.resourceregistrymanagement.exception.UnauthorizedException;
import eu.demeterh2020.resourceregistrymanagement.service.AttachmentService;
import eu.demeterh2020.resourceregistrymanagement.service.AuditService;
import eu.demeterh2020.resourceregistrymanagement.service.DehResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.geo.GeoJsonModule;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public DehResource saveDehResource(@Valid @RequestBody DehResourceForCreationDTO dehResourceForCreationDTO,
                                       @RequestHeader(value = "user-id") String userId) {

        if (userId == null) {
            log.error("User id is missing.");
            throw new BadRequestException("User id is missing");
        }


        log.info("saveDehResource called.");


        if (dehResourceService.existByName(dehResourceForCreationDTO.getName())) {
            throw new ResourceAlreadyExists("Resource with a name " + dehResourceForCreationDTO.getName() + " already exists");
        }

        DehResource converted = convertToDehResource(dehResourceForCreationDTO);
        converted.setOwner(userId);
        DehResource savedResource = dehResourceService.save(converted);

        log.info("DEH Resource saved with uid:" + savedResource.getUid());

        return savedResource;
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
    public DehResource saveDehResourceMultipartForm(@ModelAttribute @Valid DehResourceForCreationDtoMultipart dehResourceDto,
                                                    BindingResult result, @RequestHeader(value = "user-id") String userId) throws IOException {

        log.info("saveDehResource called.");

        if (userId == null) {
            log.error("User id is missing.");
            throw new BadRequestException("User id is missing");
        }

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
            String message = errors.stream().collect(Collectors.joining(" "));

            throw new BadRequestException(message);
        }

        //Check if resource with same name exists
        if (dehResourceService.existByName(dehResourceDto.getName())) {
            throw new ResourceAlreadyExists("Resource with a name " + dehResourceDto.getName() + " already exists");
        }

        List<Attachment> savedAttachments = new ArrayList<>();

        //Save attachments
        if (dehResourceDto.getAttachmentFile() != null
                && !dehResourceDto.getAttachmentFile().iterator().next().getResource().getFilename().equalsIgnoreCase("")) {
            List<MultipartFile> attachments = dehResourceDto.getAttachmentFile();
            for (MultipartFile uploadedFile : attachments) {
                String attachmentId = attachmentService.saveAttachment(uploadedFile);
                savedAttachments.add(attachmentService.getAttachment(attachmentId));
            }
        }

        DehResource converted = convertFromMultipartToDehResource(dehResourceDto);
        converted.setAttachment(savedAttachments);
        converted.setOwner(userId);
        DehResource savedResource = dehResourceService.save(converted);

        log.info("DEH Resource saved with uid:" + savedResource.getUid());

        return savedResource;
    }

    //TODO finish te deleteing of attachments form db
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
    public ResponseEntity deleteDehResource(@PathVariable("uid") String uid,
                                            @RequestHeader(value = "user-id") String userId) {

        log.info("deleteDehResource called.");

        if (userId == null) {
            log.error("User id is missing.");
            throw new BadRequestException("User id is missing");
        }
        Optional<DehResource> dehResource = dehResourceService.findOneByUid(uid);
        if (dehResource.isPresent()) { // resource exist in DB
            if (dehResource.get().getOwner().equals(userId)) {
                log.info("DEH Resource with uid:" + uid + " exist in DB.");
                dehResourceService.deleteByUid(uid);
                attachmentService.deleteAttachments(dehResource.get().getAttachment());
                return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted resource with uid: " + uid);
            }
            log.error("Access denied for resource with uid:" + uid + "for user: " + userId);
            throw new UnauthorizedException("Access denied for resource with uid: " + uid);
        }
        log.error("Resource with uid:" + uid + " not found");
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
    public DehResource partialUpdateDehResource(@PathVariable(value = "uid") String uid, @RequestBody JsonPatch patch,
                                                @RequestHeader(value = "user-id") String userId) throws JsonPatchException, JsonProcessingException {

        log.info("partialUpdateDehResource called.");

        if (userId == null) {
            log.error("User id is missing.");
            throw new BadRequestException("User id is missing");
        }

        Optional<DehResource> dehResource = dehResourceService.findOneByUid(uid);
        if (dehResource.isPresent()) { // resource exist in DB
            if (dehResource.get().getOwner().equals(userId)) {
                log.info("DEH Resource with uid:" + uid + " exist in DB.");

                DehResource dehResourcePatched = dehResourceService.partialUpdate(uid, patch);
                auditService.update(dehResourcePatched);

                log.info("DEH Resource with uid:" + uid + " patched.", dehResourcePatched);

                return dehResourcePatched;
            } else {
                log.error("Access denied for resource with uid:" + uid + "for user: " + userId);
                throw new UnauthorizedException("Access denied for resource with uid: " + uid);
            }
        }
        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");

    }

    @Operation(summary = "Update existing DEH Resource")
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
    @PutMapping(path = "/{uid}", consumes = {"application/json"})
    public DehResource updateDehResource(@PathVariable(value = "uid") String uid, @Valid @RequestBody DehResourceForCreationDTO dehResourceForUpdating,
                                         @RequestHeader(value = "user-id") String userId) {

        log.info("updateDehResource called.");

        if (userId == null) {
            log.error("User id is missing.");
            throw new BadRequestException("User id is missing");
        }

        Optional<DehResource> dehResource = dehResourceService.findOneByUid(uid);
        if (dehResource.isPresent()) { // resource exist in DB
            if (dehResource.get().getOwner().equals(userId)) {
                log.info("DEH Resource with uid:" + uid + " exist in DB.");
                dehResourceForUpdating.setOwner(userId);
                DehResource updatedDehResource = dehResourceService.update(uid, dehResourceForUpdating);
                auditService.update(updatedDehResource);

                log.info("DEH Resource with uid:" + uid + " updated.", updatedDehResource);

                return updatedDehResource;
            } else {
                log.error("Access denied for resource with uid:" + uid + "for user: " + userId);
                throw new UnauthorizedException("Access denied for resource with uid: " + uid);
            }
        }
        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
    }

    //TODO check with Marco and change the updating of attachment
    @PutMapping(path = "multipart/{uid}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public DehResource updateDehResourceMultipartForm(@PathVariable(value = "uid") String uid,
                                                      @ModelAttribute @Valid DehResourceForCreationDtoMultipart dehResourceDto,
                                                      BindingResult result, @RequestHeader(value = "user-id") String userId) throws IOException {

        log.info("updateDehResourceMultipartForm called.");

        if (userId == null) {
            log.error("User id is missing.");
            throw new BadRequestException("User id is missing");
        }

        Optional<DehResource> dehResource = dehResourceService.findOneByUid(uid);

        if (dehResource.isPresent()) { // resource exist in DB
            if (dehResource.get().getOwner().equals(userId)) {
                log.info("DEH Resource with uid:" + uid + " exist in DB.");

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

                    String message = errors.stream().collect(Collectors.joining(" "));

                    throw new BadRequestException(message);
                }

                dehResourceDto.setOwner(userId);
                DehResource updatedDehResource = dehResourceService.updateMultipartForm(uid, dehResourceDto);
                auditService.update(updatedDehResource);

                log.info("DEH Resource with uid:" + uid + " updated.", updatedDehResource);

                return updatedDehResource;
            } else {
                log.error("Access denied for resource with uid:" + uid + "for user: " + userId);
                throw new UnauthorizedException("Access denied for resource with uid: " + uid);
            }
        }
        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
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
    public DehResource findOneByUid(@PathVariable String uid,
                                    @RequestHeader(value = "user-id") String userId) {

        log.info("findOneByUid called.");

        if (userId == null) {
            log.error("User id is missing.");
            throw new BadRequestException("User id is missing");
        }

        Optional<DehResource> dehResource = dehResourceService.findOneByUid(uid);

        //TODO change implementation regarding DYMER
        if (dehResource.isPresent()) { // resource exist in DB
            if (dehResource.get().getStatus() == 1 && dehResource.get().getAccessibility() == 0) {
                log.info("DEH Resource with uid:" + uid + " exist in DB.");

                // Store history consumption
                return dehResourceService.updateNumberOfDownloads(uid);
                // Convert fetched DEHResource to DTO object
            } else {
                log.error("Access denied for resource with uid:" + uid + "for user: " + userId);
                throw new UnauthorizedException("Access denied for resource with uid: " + uid);
            }
        }
        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
    }

    @Operation(summary = "List all DEH Resources")
    @GetMapping
    public Page<DehResource> findAll(@RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                                     @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize,
                                     @RequestParam(name = "sortBy", required = false, defaultValue = "name") String sortBy,
                                     @RequestParam(name = "sortingOrder", required = false, defaultValue = "ASC") Sort.Direction sortingOrder,
                                     @RequestHeader(value = "user-id") String userId) {

        log.info("findAll called.");

        if (userId == null) {
            log.error("User id is missing.");
            throw new BadRequestException("User id is missing");
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortingOrder, sortBy);

        return dehResourceService.findAll(pageable, userId);
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
    public List<String> findAllCategories() {

        log.info("findAllCategories called.");

        return dehResourceService.findAllCategories();
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
    public List<String> findAllTypes() {

        log.info("findAllTypes called.");

        return dehResourceService.findAllTypes();
    }

    @Operation(summary = "Search for a DEH Resource by Filters")
    @GetMapping(value = "/search")
    public Page<DehResource> search(@RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                                    @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize,
                                    @RequestParam(name = "sortBy", required = false, defaultValue = "name") String sortBy,
                                    @RequestParam(name = "sortingOrder", required = false, defaultValue = "ASC") Sort.Direction sortingOrder,
                                    @RequestParam(name = "localisationDistance", required = false) String localisationDistance,
                                    @RequestParam(name = "uid", required = false) String resourceUid,
                                    @QuerydslPredicate(root = DehResource.class) Predicate predicate,
                                    @RequestHeader(value = "user-id") String userId) {

        log.info("search called.");

        if (userId == null) {
            log.error("User id is missing.");
            throw new BadRequestException("User id is missing");
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortingOrder, sortBy);

        if (localisationDistance != null) {
            log.info("Filtered search with distance");

            return dehResourceService.findAllByQuery(predicate, pageable, localisationDistance, userId);
        }
        if (resourceUid != null) {
            List<DehResource> dehResources = new ArrayList<>();
            Optional<DehResource> resource = dehResourceService.findOneByUid(resourceUid);
            if (resource.isPresent()) {
                dehResources.add(resource.get());
            }
            return new PageImpl<>(dehResources, PageRequest.of(pageable.getPageNumber()
                    , pageable.getPageSize()), 1);
        }
        return dehResourceService.findAllByQuery(predicate, pageable, userId);
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
    public DehResource rateResource(@PathVariable String uid, @RequestBody Double rating,
                                    @RequestHeader(value = "user-id") String userId) {

        log.info("rateResource called.");

        if (userId == null) {
            log.error("User id is missing.");
            throw new BadRequestException("User id is missing");
        }

        Optional<DehResource> dehResource = dehResourceService.findOneByUid(uid);

        if (dehResource.isPresent()) {
            log.info("DEH Resource with uid:" + uid + " exist in DB.");

            Double updatedRating = auditService.updateRatingByUid(uid, rating);
            dehResource.get().setRating(updatedRating);

            return dehResourceService.save(dehResource.get());
        }
        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
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
    private DehResource convertFromMultipartToDehResource(DehResourceForCreationDtoMultipart dehResourceForCreationDTO) {
        // Mapping DehResourceForCreationDTO to DehResource
        GeoJsonPoint location = new GeoJsonPoint(dehResourceForCreationDTO.getLocalisation().getX(), dehResourceForCreationDTO.getLocalisation().getY());
        DehResource dehResource = modelMapper.map(dehResourceForCreationDTO, DehResource.class);
        dehResource.getLocalisation().add(location);
        return dehResource;
    }

    private DehResource convertToDehResourceTest(String dehResource) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new GeoJsonModule());
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DehResource modelDTO = objectMapper.readValue(dehResource, DehResource.class);
        return modelDTO;
    }

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
}
