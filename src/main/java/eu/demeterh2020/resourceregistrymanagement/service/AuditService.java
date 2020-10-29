package eu.demeterh2020.resourceregistrymanagement.service;

import eu.demeterh2020.resourceregistrymanagement.domain.Audit;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuditService {

    /**
     * Method for storing a new Audit object for stored DEH Resource in DB
     *
     * @param audit - Audit data for DEH resource
     * @return stored audit object for stored DEH Resource
     */
    Audit save(Audit audit);

    /**
     * Method for updating Audit data for modified DEH Resource in DB
     *
     * @param dehResource - Updated DEHResource
     * @return updated audit object for stored DEH Resource
     */
    Audit update(DehResource dehResource);

    /**
     * Method for updating History Consumption for DEH Resource in DB
     *
     * @param resourceUid - UID for consumed DEH resource
     */
    void updateHistoryConsumptionByUid(String resourceUid);

    /**
     * Method for updating Rating of DEH Resource in DB
     *
     * @param resourceUid - UID for consumed DEH resource
     * @param rating      - Rate score for consumed DEH resource
     * @return Current rating of DEH Resource
     */
    Double updateRatingByUid(String resourceUid, Double rating);

    /**
     * Method for getting Audit data for DEH resource by UID from DB
     *
     * @param resourceUid - DEH resource UID
     * @return Audit object for DEH Resource
     */
    Optional<Audit> findOneByResourceUid(String resourceUid);

    /**
     * Method for getting History Consumption data for DEH resource by UID from DB
     *
     * @param resourceUid - DEH resource UID
     * @return List with History Consumption for specific DEH Resource
     */
    List<LocalDateTime> getHistoryConsumptionForResource(String resourceUid);

    /**
     * Method for getting all Audit for DEH Resource from DB
     *
     * @param pageable - object with defined page, size and sort
     * @return page with all Audit for DEH Resources in DB
     */
    Page<Audit> findAll(Pageable pageable);

    /**
     * Method for deleting Audit for DEH Resource by UID from DB
     *
     * @param resourceUid - DEH resource UID
     */
    void deleteByResourceUid(String resourceUid);

    /**
     * Method for checking if Audit data for DEH Resources with UID exists in DB
     *
     * @param uid - uid - DEH resource UID
     * @return true or false
     */
    boolean existByResourceUid(String uid);
}
