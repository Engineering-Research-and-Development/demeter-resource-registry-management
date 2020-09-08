package eu.demeterh2020.resourceregistrymanagement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import eu.demeterh2020.resourceregistrymanagement.domain.Audit;
import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceNotFoundException;
import eu.demeterh2020.resourceregistrymanagement.logging.Loggable;
import eu.demeterh2020.resourceregistrymanagement.repository.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    AuditRepository auditRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Audit save(Audit audit) {

        return auditRepository.save(audit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Audit update(DEHResource dehResource, String data) throws IOException {
        Optional<Audit> audit = auditRepository.findByUid(dehResource.getUid());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        JsonNode rootNode = objectMapper.readTree(data);

        audit.get().setLastUpdate(dehResource.getLastUpdate());

        if (rootNode.findValue("version") != null) { // check if version is changed
            audit.get().getVersions().put(dehResource.getVersion(), dehResource.getLastUpdate());

            return auditRepository.save(audit.get());
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void updateHistoryConsumptionByUid(String resourceUid) {
        Optional<Audit> resourceAudit = auditRepository.findByUid(resourceUid);
        if (resourceAudit.isPresent()) {
            resourceAudit.get().getConsumption().add(LocalDateTime.now());
            auditRepository.save(resourceAudit.get());
        } else {
            throw new ResourceNotFoundException("History consumption for resource with uid: " + resourceUid + " doesn't exist");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Optional<Audit> findOneByResourceUid(String resourceUid) {
        return auditRepository.findByUid(resourceUid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public List<LocalDateTime> getHistoryConsumptionForResource(String resourceUid) {
        Optional<Audit> resourceAudit = auditRepository.findByUid(resourceUid);
        if (resourceAudit.isPresent()) {

            return resourceAudit.get().getConsumption();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<Audit> findAll(Pageable pageable) {
        return auditRepository.findAll(pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void deleteByResourceUid(String resourceUid) {
        auditRepository.deleteById(resourceUid);
    }
}
