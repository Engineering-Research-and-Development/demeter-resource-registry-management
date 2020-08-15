package eu.demeterh2020.resourceregistrymanagement.service;


import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Optional;


public interface DEHResourceService {

    DEHResource saveDEHResource(DEHResource dehResource);

    DEHResource update(String uid, String data) throws IOException;

    void deleteDEHResource(String uid);

    Optional<DEHResource> findByUid(String uid);

    Page<DEHResource> findAll(Pageable pageable);

    Page<DEHResource> findAllByQuery(Predicate predicate, Pageable pageable);
}
