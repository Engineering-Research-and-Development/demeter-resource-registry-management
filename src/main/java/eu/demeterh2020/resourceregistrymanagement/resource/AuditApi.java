package eu.demeterh2020.resourceregistrymanagement.resource;

import eu.demeterh2020.resourceregistrymanagement.domain.Audit;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceNotFoundException;
import eu.demeterh2020.resourceregistrymanagement.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "api/v1/audit", produces = {MediaType.APPLICATION_JSON_VALUE})
public class AuditApi {


    private final static Logger log = LoggerFactory.getLogger(AuditApi.class);

    @Autowired
    private AuditService auditService;

    @Operation(summary = "Get audit data for all DEH Resources")
    @GetMapping
    public Page<Audit> findAll(@RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                               @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize,
                               @RequestParam(name = "sortBy", required = false, defaultValue = "name") String sortBy,
                               @RequestParam(name = "sortingOrder", required = false, defaultValue = "ASC") Sort.Direction sortingOrder) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortingOrder, sortBy);

        return auditService.findAll(pageable);
    }

    @Operation(summary = "Get audit data for DEH Resource by uid")
    @GetMapping(value = "/{uid}")
    public Audit findOneByResourceUid(@PathVariable String uid) {

        Optional<Audit> audit = auditService.findOneByResourceUid(uid);

        if (audit.isPresent()) { // audit for resource exist in DB
            return audit.get();
        }

        log.error("Audit data for resource with uid:" + uid + " not found");
        throw new ResourceNotFoundException("Audit data for resource with uid:" + uid + " not found");
    }
}
