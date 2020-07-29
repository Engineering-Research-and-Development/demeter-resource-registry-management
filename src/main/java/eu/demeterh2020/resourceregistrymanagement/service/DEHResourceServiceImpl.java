package eu.demeterh2020.resourceregistrymanagement.service;

import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;
import eu.demeterh2020.resourceregistrymanagement.repository.DEHRepository;
import eu.demeterh2020.resourceregistrymanagement.util.CompatibilityChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class DEHResourceServiceImpl implements DEHResourceService {

    @Autowired
    private DEHRepository dehRepository;

    @Autowired
    private CompatibilityChecker compatibilityChecker;

    @Override
    public DEHResource saveDEHResource(DEHResource dehResource) {
        if (compatibilityChecker.checkCompatibility(dehResource) == true) {
            return dehRepository.save(dehResource);
        }
        return null;
    }

    @Override
    public void deleteDEHResource(String uid) {
        dehRepository.deleteById(uid);
    }

    @Override
    public DEHResource findByUid(String uid) {
        return dehRepository.findByUid(uid);
    }

    @Override
    public Page<DEHResource> findAll(Pageable pageable) {
        return dehRepository.findAll(pageable);
    }

    @Override
    public Page<DEHResource> findAllByQuery(Predicate predicate, Pageable pageable) {
        return dehRepository.findAll(predicate, pageable);
    }
}
