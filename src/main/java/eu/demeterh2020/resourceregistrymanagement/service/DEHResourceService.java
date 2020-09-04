package eu.demeterh2020.resourceregistrymanagement.service;


import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Optional;


public interface DEHResourceService {

    /**
     * Method for storing a new DEH Resource in DB
     *
     * @param dehResource - DEH resource for storing
     * @return stored DEH Resource
     */
    DEHResource save(DEHResource dehResource);

    /**
     * Method for updating existing DEH Resource in DB
     *
     * @param uid  - DEH resource uid
     * @param data - String with modified fields in DEH Resource
     * @return updated DEH resource
     */
    DEHResource update(String uid, String data) throws IOException;


    /**
     * Method for deleting DEH Resource by UID from DB
     *
     * @param uid - DEH resource UID
     */
    void deleteByUid(String uid);

    /**
     * Method for getting DEH Resource by UID from DB
     *
     * @param uid - DEH resource UID
     * @return Optional DEH Resource
     */
    Optional<DEHResource> findOneByUid(String uid);

    /**
     * Method for checking if DEH Resources with UID exists in DB
     *
     * @param uid - uid - DEH resource UID
     * @return true or false
     */
    boolean existByUid(String uid);

    /**
     * Method for getting all DEH Resources from DB
     *
     * @param pageable - object with defined page, size and sort
     * @return page with all DEH Resources in DB
     */
    Page<DEHResource> findAll(Pageable pageable);

    /**
     * Method for getting all DEH Resources which corresponds to specific criteria/filter from  from DB
     *
     * @param predicate - object with criteria
     * @param pageable  - object with defined page, size and sort
     * @return page with all DEH Resources in DB
     */
    Page<DEHResource> findAllByQuery(Predicate predicate, Pageable pageable);
}
