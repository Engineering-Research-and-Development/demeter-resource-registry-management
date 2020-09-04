package eu.demeterh2020.resourceregistrymanagement.resource;

import eu.demeterh2020.resourceregistrymanagement.domain.Audit;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceNotFoundException;
import eu.demeterh2020.resourceregistrymanagement.service.AuditService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "api/v1/audit", produces = {MediaType.APPLICATION_JSON_VALUE})
@Api(value = "Audit REST API")
public class AuditApi {


    private final static Logger log = LoggerFactory.getLogger(AuditApi.class);

    @Autowired
    private AuditService auditService;


    @GetMapping
    public Page<Audit> findAll(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                               @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                               @RequestParam(name = "sort", required = false, defaultValue = "name,desc") String sort,
                               Pageable pageable) {

        return auditService.findAll(pageable);
    }

    @GetMapping(value = "/{uid}")
    public Audit findOneByUid(@PathVariable String uid) {

        Optional<Audit> audit = auditService.findOneByResourceUid(uid);

        if (audit.isPresent()) { // audit for resource exist in DB
            return audit.get();
        }
        log.error("Audit data for resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Audit data for resource with uid:" + uid + " not found");
    }
}
