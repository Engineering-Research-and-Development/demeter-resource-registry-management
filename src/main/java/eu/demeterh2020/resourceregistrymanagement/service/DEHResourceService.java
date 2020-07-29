package eu.demeterh2020.resourceregistrymanagement.service;


import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface DEHResourceService {

    /**
     * @param dehResource
     * @return
     */
    DEHResource saveDEHResource(DEHResource dehResource);

    void deleteDEHResource(String uid);

    DEHResource findByUid(String uid);

    Page<DEHResource> findAll(Pageable pageable);

    Page<DEHResource> findAllByQuery(Predicate predicate, Pageable pageable);
}
