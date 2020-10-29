package eu.demeterh2020.resourceregistrymanagement.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.DehResourceForCreationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


public interface DehResourceService {

    /**
     * Method for storing a new DEH Resource in DB
     *
     * @param dehResource - DEH resource for storing
     * @return stored DEH Resource
     */
    DehResource save(DehResource dehResource);

    /**
     * Method for partial updating existing DEH Resource in DB
     *
     * @param uid   - DEH resource uid
     * @param patch - JsonPatch with modified fields and operations in DEH Resource
     * @return updated DEH resource
     */
    DehResource partialUpdate(String uid, JsonPatch patch) throws JsonPatchException, JsonProcessingException;


    /**
     * Method for updating existing DEH Resource in DB
     *
     * @param uid                    - DEH resource uid
     * @param dehResourceForUpdating - DehResource with modified fields
     * @return updated DEH resource
     */
    DehResource update(String uid, DehResourceForCreationDTO dehResourceForUpdating);

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
    Optional<DehResource> findOneByUid(String uid);

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
    Page<DehResource> findAll(Pageable pageable);

    /**
     * Method for getting all DEH Resources which corresponds to specific criteria/filter from  from DB
     *
     * @param predicate - object with criteria
     * @param pageable  - object with defined page, size and sort
     * @return page with all DEH Resources in DB
     */
    Page<DehResource> findAllByQuery(Predicate predicate, Pageable pageable);

    /**
     * Method for getting all DEH Resources which corresponds to specific criteria/filter from  from DB, and calculates
     *
     * @param predicate            - object with criteria
     * @param pageable             - object with defined page, size and sort
     * @param localisationDistance string of localisation request given in format "latitude,longitude,distance"
     * @return page with all DEH Resources in DB
     */
    Page<DehResource> findAllByQuery(Predicate predicate, Pageable pageable, String localisationDistance);

    /**
     * Method for getting all names categories of DEH Resources stored in DB
     *
     * @return list with all DEH Resources categories in DB
     */
    List<String> findAllCategories();

    /**
     * Method for getting all names types of DEH Resources stored in DB
     *
     * @return list with all DEH Resources types in DB
     */
    List<String> findAllTypes();

    //TODO Change implementation according to DEH Client

    /**
     * Method for updating History Consumption for DEH Resource in DB
     *
     * @param resourceUid - UID for consumed DEH resource
     */
    DehResource updateNumberOfDownloads(String resourceUid);
}
