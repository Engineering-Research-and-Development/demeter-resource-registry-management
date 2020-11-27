package eu.demeterh2020.resourceregistrymanagement.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.DehResourceForCreationDTO;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private ModelMapper modelMapper;


    @Operation(summary = "Register new DEH Resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource registered",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DehResourceForCreationDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @PostMapping
    public DehResource saveDehResource(@RequestBody DehResourceForCreationDTO dehResourceForCreationDTO) {

        log.info("saveDehResource called.");
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
    public ResponseEntity deleteDehResource(@PathVariable("uid") String uid) {

        log.info("deleteDehResource called.");

        if (dehResourceService.existByUid(uid)) { // resource exist in DB
            log.info("DEH Resource with uid:" + uid + " exist in DB.");
            dehResourceService.deleteByUid(uid);

            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted resource with uid: " + uid);
        }

        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
    }

    @Operation(summary = "Partial update existing DEH Resource")
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
    public DehResource partialUpdateDehResource(@PathVariable(value = "uid") String uid, @RequestBody JsonPatch patch) throws JsonPatchException, JsonProcessingException {

        log.info("partialUpdateDehResource called.");

        if (dehResourceService.existByUid(uid)) { // resource exist in DB
            log.info("DEH Resource with uid:" + uid + " exist in DB.");

            DehResource dehResourcePatched = dehResourceService.partialUpdate(uid, patch);
            auditService.update(dehResourcePatched);

            log.info("DEH Resource with uid:" + uid + " patched.", dehResourcePatched);

            return dehResourcePatched;

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
    public DehResource updateDehResource(@PathVariable(value = "uid") String uid, @RequestBody DehResourceForCreationDTO dehResourceForUpdating) throws JsonPatchException, JsonProcessingException {

        log.info("updateDehResource called.");

        if (dehResourceService.existByUid(uid)) { // resource exist in DB
            log.info("DEH Resource with uid:" + uid + " exist in DB.");

            DehResource updatedDehResource = dehResourceService.update(uid, dehResourceForUpdating);
            auditService.update(updatedDehResource);

            log.info("DEH Resource with uid:" + uid + " updated.", updatedDehResource);

            return updatedDehResource;
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
    public DehResource findOneByUid(@PathVariable String uid) {

        log.info("findOneByUid called.");

        Optional<DehResource> dehResource = dehResourceService.findOneByUid(uid);

        //TODO change implementation regarding DYMER
        if (dehResource.isPresent()) { // resource exist in DB
            log.info("DEH Resource with uid:" + uid + " exist in DB.");

            // Store history consumption
            return dehResourceService.updateNumberOfDownloads(uid);
            // Convert fetched DEHResource to DTO object

        }
        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
    }

    @Operation(summary = "List all DEH Resources")
    @GetMapping
    public Page<DehResource> findAll(@RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                                     @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize,
                                     @RequestParam(name = "sortBy", required = false, defaultValue = "name") String sortBy,
                                     @RequestParam(name = "sortingOrder", required = false, defaultValue = "ASC") Sort.Direction sortingOrder) {

        log.info("findAll called.");

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortingOrder, sortBy);

        return dehResourceService.findAll(pageable);
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
    @Operation(summary = "List all names of DEH Resources Categories")
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
    @Operation(summary = "List all names of DEH Resources Types")
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
                                    @RequestParam(name = "sortingOrder", required = false, defaultValue = "DESC") Sort.Direction sortingOrder,
                                    @RequestParam(name = "localisationDistance", required = false) String localisationDistance,
                                    @QuerydslPredicate(root = DehResource.class) Predicate predicate) {

        log.info("search called.");

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortingOrder, sortBy);

        if (localisationDistance != null) {
            log.info("Filtered search with distance");

            return dehResourceService.findAllByQuery(predicate, pageable, localisationDistance);
        }
        return dehResourceService.findAllByQuery(predicate, pageable);
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
    public DehResource rateResource(@PathVariable String uid, @RequestBody Double rating) {

        log.info("rateResource called.");

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
}
