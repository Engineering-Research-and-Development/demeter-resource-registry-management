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
import java.util.Optional;


@Service
public class DEHResourceServiceImpl implements DEHResourceService {

    @Autowired
    private DEHRepository dehRepository;

    @Autowired
    private CompatibilityChecker compatibilityChecker;

    @Override
    @Loggable
    public DEHResource saveDEHResource(DEHResource dehResource) {

        if (compatibilityChecker.checkCompatibility(dehResource) == true) {
            return dehRepository.save(dehResource);
        }
        return null;
    }

    @Override
    public DEHResource update(String uid, String data) throws IOException {

        DEHResource dehResource = dehRepository.findByUid(uid).orElse(null);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        ObjectReader objectReader = objectMapper.readerForUpdating(dehResource);
        DEHResource updatedDehResource = objectReader.readValue(data, DEHResource.class);

        return dehRepository.save(updatedDehResource);

    }

    @Override
    @Loggable
    public void deleteDEHResource(String uid) {
        dehRepository.deleteById(uid);
    }

    @Override
    @Loggable
    public Optional<DEHResource> findByUid(String uid) {

        Optional<DEHResource> dehResource = dehRepository.findByUid(uid);

        return dehResource;
    }

    @Override
    @Loggable
    public Page<DEHResource> findAll(Pageable pageable) {

        return dehRepository.findAll(pageable);
    }

    @Override
    @Loggable
    public Page<DEHResource> findAllByQuery(Predicate predicate, Pageable pageable) {

        return dehRepository.findAll(predicate, pageable);
    }
}
