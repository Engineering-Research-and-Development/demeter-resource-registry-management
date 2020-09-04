package eu.demeterh2020.resourceregistrymanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;
import eu.demeterh2020.resourceregistrymanagement.logging.Loggable;
import eu.demeterh2020.resourceregistrymanagement.repository.DEHRepository;
import eu.demeterh2020.resourceregistrymanagement.util.CompatibilityChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class DEHResourceServiceImpl implements DEHResourceService {

    @Autowired
    private DEHRepository dehRepository;

    @Autowired
    private CompatibilityChecker compatibilityChecker;

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DEHResource save(DEHResource dehResource) {

        // Check if deh resource is compatible
        if (compatibilityChecker.checkCompatibility(dehResource) == true) {
            // Set creating time of DEH Resource
            dehResource.setCreateAt(LocalDateTime.now());
            // Store DEH Resource in DB
            return dehRepository.save(dehResource);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DEHResource update(String uid, String data) throws IOException {
        // Get DEH Resource from DB by UID
        DEHResource dehResource = dehRepository.findByUid(uid).orElse(null);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        ObjectReader objectReader = objectMapper.readerForUpdating(dehResource);
        DEHResource updatedDehResource = objectReader.readValue(data, DEHResource.class);
        updatedDehResource.setLastUpdate(LocalDateTime.now());

        return dehRepository.save(updatedDehResource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void deleteByUid(String uid) {
        dehRepository.deleteById(uid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Optional<DEHResource> findOneByUid(String uid) {

        Optional<DEHResource> dehResource = dehRepository.findByUid(uid);

        return dehResource;
    }

    @Override
    public boolean existByUid(String uid) {
        return dehRepository.existsByUid(uid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<DEHResource> findAll(Pageable pageable) {

        return dehRepository.findAll(pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<DEHResource> findAllByQuery(Predicate predicate, Pageable pageable) {

        return dehRepository.findAll(predicate, pageable);
    }
}
