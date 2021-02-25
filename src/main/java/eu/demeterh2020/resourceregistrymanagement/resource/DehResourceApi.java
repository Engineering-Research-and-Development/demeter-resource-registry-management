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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                                       @RequestHeader(value = "user-id") String userId) throws IOException {

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
        // Store DEH resource in DB
//        if (file != null){
//            String attachmentId = attachmentService.saveAttachment(file);
//            Attachment savedAttachment = attachmentService.getAttachment(attachmentId);
//            converted.setAttachment(savedAttachment);
//        }
        DehResource savedResource = dehResourceService.save(converted);

        log.info("DEH Resource saved with uid:" + savedResource.getUid());

        return savedResource;
    }

    @Operation(hidden = true, summary = "Register new DEH Resource Test Multipart form")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource registered",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DehResourceForCreationDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @PostMapping(path = "/testString", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DehResource saveDehResourceMultipartForm(@RequestPart(value = "name", required = true) String name,
                                                    @RequestPart(value = "type", required = true) String type,
                                                    @RequestPart(value = "category", required = false) List<String> category,
                                                    @RequestPart(value = "description", required = false) String description,
                                                    @RequestPart(value = "endpoint", required = false) String endpoint,
                                                    @RequestPart(value = "status", required = true) int status,
                                                    @RequestPart(value = "version", required = false) String version,
                                                    @RequestPart(value = "maturityLevel", required = false) int maturityLevel,
                                                    @RequestPart(value = "owner", required = false) String owner,
                                                    @RequestPart(value = "tags", required = false) List<String> tags,
                                                    @RequestPart(value = "attachment", required = false) MultipartFile attachment,
                                                    @RequestPart(value = "localisation", required = false) List<GeoJsonPoint> localisation,
                                                    @RequestPart(value = "accessibility", required = false) int accessibility,
                                                    @RequestPart(value = "dependencies", required = false) List<String> dependencies,
                                                    @RequestPart(value = "accessControlPolicies", required = false) List<String> accessControlPolicies,
                                                    @RequestPart(value = "url", required = false) String url) throws IOException {

        log.info("saveDehResource called.");
        Attachment savedAttachment = null;
        if (attachment != null) {
            String attachmentId = attachmentService.saveAttachment(attachment);
            savedAttachment = attachmentService.getAttachment(attachmentId);
        }
//        DehResource converted = convertToDehResource(dehResourceForCreationDTO);
        DehResourceForCreationDTO dehResourceForCreationDTO = new DehResourceForCreationDTO(name, type, category, description, endpoint, status, version, maturityLevel, owner, tags, savedAttachment, localisation, accessibility, dependencies, accessControlPolicies, url);
        // Store DEH resource in DB

        DehResource converted = convertToDehResource(dehResourceForCreationDTO);

        DehResource savedResource = dehResourceService.save(converted);

        log.info("DEH Resource saved with uid:" + savedResource.getUid());

        return savedResource;
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
                                         @RequestHeader(value = "user-id") String userId) throws JsonPatchException, JsonProcessingException {

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

    private DehResource converToDehResourceTest(String dehResource) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new GeoJsonModule());
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DehResource modelDTO = objectMapper.readValue(dehResource, DehResource.class);
        return modelDTO;
    }
}
