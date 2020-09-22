package eu.demeterh2020.resourceregistrymanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.logging.Loggable;
import eu.demeterh2020.resourceregistrymanagement.repository.DehRepository;
import eu.demeterh2020.resourceregistrymanagement.util.CompatibilityChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class DehResourceServiceImpl implements DehResourceService {

    @Autowired
    private DehRepository dehRepository;

    @Autowired
    private CompatibilityChecker compatibilityChecker;

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DehResource save(DehResource dehResource) {

        Assert.notNull(dehResource, "DEHResource must not be null!");

        // Check if  DEHresource is compatible
        if (compatibilityChecker.checkCompatibility(dehResource) == true) {
            // Set creating time of DEHResource
            dehResource.setCreateAt(LocalDateTime.now());
            // Store DEHResource in DB
            return dehRepository.save(dehResource);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DehResource update(String uid, String data) throws IOException {

        Assert.hasText(uid, "DEHResource uid must not be null!");
        Assert.notNull(data, "DEHResource update data must not be null!");

        // Get DEHResource from DB by uid
        DehResource dehResource = dehRepository.findByUid(uid).orElse(null);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        ObjectReader objectReader = objectMapper.readerForUpdating(dehResource);
        DehResource updatedDehResource = objectReader.readValue(data, DehResource.class);
        updatedDehResource.setLastUpdate(LocalDateTime.now());

        return dehRepository.save(updatedDehResource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void deleteByUid(String uid) {

        Assert.hasText(uid, "DEHResource uid must not be null!");

        dehRepository.deleteById(uid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Optional<DehResource> findOneByUid(String uid) {

        Assert.hasText(uid, "DEHResource uid must not be null!");

        Optional<DehResource> dehResource = dehRepository.findByUid(uid);

        return dehResource;
    }

    @Override
    public boolean existByUid(String uid) {

        Assert.hasText(uid, "DEHResource uid must not be null!");

        return dehRepository.existsByUid(uid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<DehResource> findAll(Pageable pageable) {

        return dehRepository.findAll(pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<DehResource> findAllByQuery(Predicate predicate, Pageable pageable) {

        Assert.notNull(pageable, "Paging criteria must not be null!");

        return dehRepository.findAll(predicate, pageable);
    }
}
