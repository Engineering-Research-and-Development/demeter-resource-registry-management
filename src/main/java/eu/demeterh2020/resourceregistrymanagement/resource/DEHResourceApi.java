package eu.demeterh2020.resourceregistrymanagement.resource;

import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceNotFoundException;
import eu.demeterh2020.resourceregistrymanagement.service.DEHResourceService;
import io.swagger.annotations.Api;
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

    @Autowired
    private DEHResourceService dehResourceService;

    @PostMapping
    public DEHResource saveDehResource(@RequestBody DEHResource dehResource) {

        return dehResourceService.saveDEHResource(dehResource);
    }

    @DeleteMapping(value = "/{uid}")
    public ResponseEntity deleteDehResource(@PathVariable("uid") String uid) {

        Optional<DEHResource> dehResource = dehResourceService.findByUid(uid);

        if (!dehResource.isPresent()) {
            throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
        } else {
            dehResourceService.deleteDEHResource(uid);

            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted resource with uid: " + uid);
        }
    }

    @PatchMapping(path = "/{uid}", consumes = {"application/merge-patch+json"})
    public ResponseEntity<DEHResource> updateUserDetails(@PathVariable(value = "uid") String uid, @RequestBody String data) throws IOException {

        Optional<DEHResource> dehResource = dehResourceService.findByUid(uid);

        if (!dehResource.isPresent()) {
            throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
        } else {
            DEHResource userDetails = dehResourceService.update(uid, data);

            return ResponseEntity.status(HttpStatus.OK).body(userDetails);
        }
    }

    @GetMapping(value = "/{uid}")
    public Optional<DEHResource> findOneByUid(@PathVariable String uid) {

        Optional<DEHResource> dehResource = dehResourceService.findByUid(uid);

        if (!dehResource.isPresent()) {
            throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
        } else {
            return dehResource;
        }
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
                                    @RequestParam(name = "accessibility", required = false) String accessibility,
                                    @RequestParam(name = "url", required = false) String url,
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
}
