package eu.demeterh2020.resourceregistrymanagement.resource;

import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.DehResourceDTO;
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

import java.io.IOException;
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
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DehResourceForCreationDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content) })
    @PostMapping
    public DehResource saveDehResource(@RequestBody DehResourceForCreationDTO dehResourceForCreationDTO) {
        // Store DEH resource in DB
        DehResource savedResource = dehResourceService.save(convertToDehResource(dehResourceForCreationDTO));

        return savedResource;
    }

    @Operation(summary = "Delete existing DEH Resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource deleted",
                    content =  @Content),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DEH Resource not found",
                    content = @Content) })
    @DeleteMapping(value = "/{uid}")
    public ResponseEntity deleteDehResource(@PathVariable("uid") String uid) {

        Optional<DehResource> dehResource = dehResourceService.findOneByUid(uid);

        if (dehResource.isPresent()) { // resource exist in DB
            dehResourceService.deleteByUid(uid);

            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted resource with uid: " + uid);
        }

        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
    }

    // TODO Change Implementation
    @Operation(summary = "Update existing DEH Resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DehResourceDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DEH Resource not found",
                    content = @Content) })
    @PatchMapping(path = "/{uid}", consumes = {"application/merge-patch+json"})
    public DehResourceDTO updateResource(@PathVariable(value = "uid") String uid, @RequestBody String data) throws IOException {

        Optional<DehResource> dehResource = dehResourceService.findOneByUid(uid);

        if (dehResource.isPresent()) { // resource exist in DB
            DehResource updatedResource = dehResourceService.update(uid, data);
            // Update Audit data for DEH Resource
            auditService.update(updatedResource, data);

            return convertToDto(updatedResource);
        }
        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");

    }

    @Operation(summary = "Find DEH Resource by uid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DehResourceDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DEH Resource not found",
                    content = @Content) })
    @GetMapping(value = "/{uid}")
    public DehResourceDTO findOneByUid(@PathVariable String uid) {

        Optional<DehResource> dehResource = dehResourceService.findOneByUid(uid);

        if (dehResource.isPresent()) { // resource exist in DB
            // Store history consumption
            auditService.updateHistoryConsumptionByUid(uid);
            // Convert fetched DEHResource to DTO object
            return convertToDto(dehResource.get());
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

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortingOrder, sortBy);

        return dehResourceService.findAll(pageable);
    }


    @Operation(summary = "Search for a DEH Resource by Filters")
    @GetMapping(value = "/search")
    public Page<DehResource> search(@RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                                    @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize,
                                    @RequestParam(name = "sortBy", required = false, defaultValue = "name") String sortBy,
                                    @RequestParam(name = "sortingOrder", required = false, defaultValue = "DESC") Sort.Direction sortingOrder,
                                    @RequestParam(name = "name", required = false) String name,
                                    @RequestParam(name = "type", required = false) String type,
                                    @RequestParam(name = "description", required = false) String description,
                                    @RequestParam(name = "endpoint", required = false) String endpoint,
                                    @RequestParam(name = "status", required = false) String status,
                                    @RequestParam(name = "version", required = false) String version,
                                    @RequestParam(name = "owner", required = false) String owner,
                                    @RequestParam(name = "rating", required = false) Double rating,
                                    @RequestParam(name = "url", required = false) String url,
                                    @RequestParam(name = "accessibility", required = false) Integer accessibility,
                                    @RequestParam(name = "maturityLevel", required = false) Integer maturityLevel,
                                    @RequestParam(name = "localisationDistance", required = false) String localisationDistance,
                                    //TODO Finish  after holiday binding for advanced search for next params
//                                    @RequestParam(name = "category", required = false) String category,
//                                    @RequestParam(name = "tags", required = false) String tags,
//                                    @RequestParam(name = "localisation", required = false) String localisation,
//                                    @RequestParam(name = "lastUpdate", required = false) String lastUpdate,
//                                    @RequestParam(name = "dependencies", required = false) String dependencies,
//                                    @RequestParam(name = "accessControlPolicies", required = false) String accessControlPolicies,
//                                    @RequestParam(name = "billingInformation", required = false) String billingInformation,

                                    @QuerydslPredicate(root = DehResource.class) Predicate predicate) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortingOrder, sortBy);

        if (localisationDistance!=null){

            return dehResourceService.findAllByQuery(predicate, pageable, localisationDistance);
        }

        return dehResourceService.findAllByQuery(predicate, pageable);
    }



    @Operation(summary = "Rate DEH Resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource rated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DehResource.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DEH Resource not found",
                    content = @Content) })
    @PostMapping(value = "{uid}/rate")
    public DehResource rateResource(@PathVariable String uid, @RequestBody String rating) {

        //TODO finish
        return null;
    }

    /**
     * Private method for converting DEHResource to DTO object which adds History Consumption form audit data
     */
    private DehResourceDTO convertToDto(DehResource dehResource) {
        // Mapping DEHResource to existing fields in DEHResourceDTO
        DehResourceDTO dehResourceDto = modelMapper.map(dehResource, DehResourceDTO.class);
        //Set consumption History for DEHResource
        dehResourceDto.setHistoryConsumption(auditService.getHistoryConsumptionForResource(dehResource.getUid()));

        return dehResourceDto;
    }

    private DehResource convertToDehResource(DehResourceForCreationDTO dehResourceForCreationDTO) {
        // Mapping DehResourceForCreationDTO to DehResource
        DehResource dehResource = modelMapper.map(dehResourceForCreationDTO, DehResource.class);
        return dehResource;
    }
}
