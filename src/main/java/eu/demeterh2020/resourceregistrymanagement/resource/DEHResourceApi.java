package eu.demeterh2020.resourceregistrymanagement.resource;

import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;
import eu.demeterh2020.resourceregistrymanagement.service.DEHResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v1/resources", produces = {MediaType.APPLICATION_JSON_VALUE})
public class DEHResourceApi extends BaseResource {

    @Autowired
    private DEHResourceService dehResourceService;

    @PostMapping
    public DEHResource saveDehResource(@RequestBody DEHResource dehResource) {
        return dehResourceService.saveDEHResource(dehResource);
    }

    @DeleteMapping(value = "/{uid}")
    public void deleteDehResource(@PathVariable("uid") String uid) {
        dehResourceService.deleteDEHResource(uid);
    }

    @GetMapping(value = "/{uid}")
    public DEHResource findOneByUid(@PathVariable String uid) {
        return dehResourceService.findByUid(uid);
    }

    @GetMapping
    public Page<DEHResource> findAll(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                     @RequestParam(name = "size", required = false, defaultValue = "1") int size,
                                     @RequestParam(name = "sort", required = false, defaultValue = "name") String sort,
                                     @RequestParam(name = "sort.dir", required = false, defaultValue = "desc") String dir,
                                     Pageable pageable) {
        return dehResourceService.findAll(pageable);
    }

    @GetMapping(value = "/search")
    public Page<DEHResource> search(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                    @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                    @RequestParam(name = "sort", required = false, defaultValue = "name") String sort,
                                    @RequestParam(name = "sort.dir", required = false, defaultValue = "desc") String dir,
                                    Pageable pageable,
                                    @QuerydslPredicate(root = DEHResource.class) Predicate predicate) {
        return dehResourceService.findAllByQuery(predicate, pageable);

    }

}
