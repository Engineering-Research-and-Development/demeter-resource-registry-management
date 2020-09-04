package eu.demeterh2020.resourceregistrymanagement.resource;

import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.DEHResourceDTO;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceNotFoundException;
import eu.demeterh2020.resourceregistrymanagement.service.AuditService;
import eu.demeterh2020.resourceregistrymanagement.service.DEHResourceService;
import io.swagger.annotations.Api;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping(value = "api/v1/resources", produces = {MediaType.APPLICATION_JSON_VALUE})
@Api(value = "DEH Resources REST API")
public class DEHResourceApi {

    private final static Logger log = LoggerFactory.getLogger(DEHResourceApi.class);

    @Autowired
    private DEHResourceService dehResourceService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ModelMapper modelMapper;


    @PostMapping
    public DEHResource saveDehResource(@RequestBody DEHResource dehResource) {
        // Store DEH resource in DB
        DEHResource savedResource = dehResourceService.save(dehResource);

        // Create audit data for stored DEH Resource
        auditService.save(savedResource);

        return savedResource;
    }

    @DeleteMapping(value = "/{uid}")
    public ResponseEntity deleteDehResource(@PathVariable("uid") String uid) {

        Optional<DEHResource> dehResource = dehResourceService.findOneByUid(uid);

        if (dehResource.isPresent()) { // resource exist in DB
            dehResourceService.deleteByUid(uid);
            if (!dehResourceService.existByUid(uid)) { // verify that resource is deleted
                auditService.deleteByResourceUid(uid);
            }

            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted resource with uid: " + uid);
        }

        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
    }

    @PatchMapping(path = "/{uid}", consumes = {"application/merge-patch+json"})
    public DEHResourceDTO updateResource(@PathVariable(value = "uid") String uid, @RequestBody String data) throws IOException {

        Optional<DEHResource> dehResource = dehResourceService.findOneByUid(uid);

        if (dehResource.isPresent()) { // resource exist in DB
            DEHResource updatedResource = dehResourceService.update(uid, data);
            // Update Audit data for DEH Resource
            auditService.update(updatedResource, data);

            return convertToDto(updatedResource);
        }
        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");

    }

    @GetMapping(value = "/{uid}")
    public DEHResourceDTO findOneByUid(@PathVariable String uid) {

        Optional<DEHResource> dehResource = dehResourceService.findOneByUid(uid);

        if (dehResource.isPresent()) { // resource exist in DB
            // Covert fethed resoruce to DTO object
            return convertToDto(dehResource.get());
        }
        log.error("Resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
    }


    @GetMapping
    public Page<DEHResource> findAll(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                     @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                     @RequestParam(name = "sort", required = false, defaultValue = "name,desc") String sort,
                                     Pageable pageable) {

        return dehResourceService.findAll(pageable);
    }

    @GetMapping(value = "/search")
    public Page<DEHResource> search(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                    @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                    @RequestParam(name = "sort", required = false, defaultValue = "name,desc") String sort,
                                    @RequestParam(name = "name", required = false) String name,
                                    @RequestParam(name = "type", required = false) String type,
                                    @RequestParam(name = "description", required = false) String description,
                                    @RequestParam(name = "endpoint", required = false) String endpoint,
                                    @RequestParam(name = "status", required = false) String status,
                                    @RequestParam(name = "version", required = false) String version,
                                    @RequestParam(name = "owner", required = false) String owner,
                                    @RequestParam(name = "rating", required = false) Double rating,
                                    @RequestParam(name = "url", required = false) String url,
                                    @RequestParam(name = "accessibility", required = false) int accessibility,
                                    //TODO Finish  after holiday binding for advanced search for next params
//                                    @RequestParam(name = "category", required = false) String category,
//                                    @RequestParam(name = "tags", required = false) String tags,
//                                    @RequestParam(name = "localisation", required = false) String localisation,
//                                    @RequestParam(name = "lastUpdate", required = false) String lastUpdate,
//                                    @RequestParam(name = "dependencies", required = false) String dependencies,
//                                    @RequestParam(name = "accessControlPolicies", required = false) String accessControlPolicies,
//                                    @RequestParam(name = "billingInformation", required = false) String billingInformation,
//                                    @RequestParam(name = "maturityLevel", required = false) int maturityLevel,

                                    Pageable pageable,
                                    @QuerydslPredicate(root = DEHResource.class) Predicate predicate) {

        return dehResourceService.findAllByQuery(predicate, pageable);
    }

    @PostMapping(value = "{uid}/rate")
    public DEHResource rateResource(@PathVariable String uid) {

        //TODO finish
        return null;
    }

    @GetMapping(value = "{uid}/download")
    public void download(@PathVariable String uid) {

        Optional<DEHResource> dehResource = dehResourceService.findOneByUid(uid);

        if (dehResource.isPresent()) {
            //TODO implement download steps
            auditService.updateHistoryConsumptionByUid(uid);

        } else {
            log.error("Resource with uid:" + uid + " not found");
            throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
        }
    }

    /**
     * Private method for converting DEHResource to DTO object which adds History Consumption form audit data
     */
    private DEHResourceDTO convertToDto(DEHResource dehResource) {
        // Mapping DEHResource to existing fields in DEHResourceDTO
        DEHResourceDTO dehResourceDto = modelMapper.map(dehResource, DEHResourceDTO.class);
        //Set consumption History for DEHResource
        dehResourceDto.setHistoryConsumption(auditService.getHistoryConsumptionForResource(dehResource.getUid()));

        return dehResourceDto;
    }
}

