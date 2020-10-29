package eu.demeterh2020.resourceregistrymanagement.service;

import com.mysema.commons.lang.Assert;
import eu.demeterh2020.resourceregistrymanagement.domain.Audit;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.logging.Loggable;
import eu.demeterh2020.resourceregistrymanagement.repository.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuditServiceImpl implements AuditService {

    private final static Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    @Autowired
    AuditRepository auditRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Audit save(Audit audit) {

        log.info("Saving audit data for DEHResource with uid:" + audit.getUid());

        return auditRepository.save(audit);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Audit update(DehResource dehResource) {

        log.info("Updating audit data for DEHResource with uid:" + dehResource.getUid());

        Assert.notNull(dehResource, "DEH Resource must not be null!");

        Optional<Audit> dehResourceAudit = auditRepository.findByUid(dehResource.getUid());

        if (!dehResourceAudit.get().getVersions().containsKey(dehResource.getVersion())) {
            dehResourceAudit.get().getVersions().put(dehResource.getVersion(), dehResource.getLastUpdate());

            return auditRepository.save(dehResourceAudit.get());
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void updateHistoryConsumptionByUid(String resourceUid) {

        log.info("Updating historyConsumption in audit data for DEHResource with uid:" + resourceUid);

        Assert.hasText(resourceUid, "DEHResource uid must not be null!");

        Optional<Audit> resourceAudit = auditRepository.findByUid(resourceUid);

        if (resourceAudit.isPresent()) {
            resourceAudit.get().getConsumption().add(LocalDateTime.now());
            auditRepository.save(resourceAudit.get());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double updateRatingByUid(String resourceUid, Double rating) {

        log.info("Updating rating in audit data for DEHResource with uid:" + resourceUid);

        Assert.hasText(resourceUid, "DEHResource uid must not be null!");
        Assert.notNull(rating, "Rating must not be null!");

        Optional<Audit> resourceAudit = auditRepository.findByUid(resourceUid);
        if (resourceAudit.isPresent()) {
            resourceAudit.get().getRating().add(rating);
            Audit savedAudit = auditRepository.save(resourceAudit.get());
            Double roundedRating = Math.round(savedAudit.getRating().stream().mapToDouble(e -> e / savedAudit.getRating().size()).sum() * 10) / 10.0;
            log.info("Rounded rating: " + roundedRating);

            return roundedRating;
        }

        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Optional<Audit> findOneByResourceUid(String resourceUid) {

        log.info("Fetching audit data for DEHResource with uid:" + resourceUid);

        Assert.hasText(resourceUid, "DEHResource uid must not be null!");

        return auditRepository.findByUid(resourceUid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public List<LocalDateTime> getHistoryConsumptionForResource(String resourceUid) {

        log.info("Fetching historyConsumption from audit data for DEHResource with uid:" + resourceUid);

        Assert.hasText(resourceUid, "DEHResource uid must not be null!");

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

        log.info("Fetching all audit data from DB");

        return auditRepository.findAll(pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void deleteByResourceUid(String resourceUid) {

        log.info("Deleting audit data for DEHResource with uid:" + resourceUid);

        Assert.hasText(resourceUid, "DEHResource uid must not be null!");

        auditRepository.deleteByUid(resourceUid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existByResourceUid(String resourceUid) {

        log.info("Checking if audit data exists for DEHResource with uid:" + resourceUid);

        Assert.hasText(resourceUid, "DEHResource uid must not be null!");

        return auditRepository.existsByUid(resourceUid);
    }
}
