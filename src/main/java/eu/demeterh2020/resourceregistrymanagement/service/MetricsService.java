package eu.demeterh2020.resourceregistrymanagement.service;

import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.Metrics;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.MetricsDataDto;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.MetricsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface MetricsService {

    /**
     * Method for storing a new metrics of DEH Resource in DB
     *
     * @param metrics - Metrics of DEH resource for storing
     */
    void save(List<MetricsDataDto> metrics);

    /**
     * Method for deleting all metrics of DEH Resource by UID from DB
     *
     * @param rrmId - Name of running container of DEH Resource
     */
    void deleteByRrmId(String rrmId);

    /**
     * Method for getting all metrics of specific container by Container id from DB
     *
     * @param containerId - Name of running container of DEH Resource
     * @return Metrics for specific Container
     */
    MetricsDataDto findOneByContainerId(String containerId);

    /**
     * Method for getting all metrics of DEH Resource by RRM UID from DB
     *
     * @param rrmId - DEH resource UID
     * @return All metrics for given DEH Resource
     */
    Metrics findOneByRrmId(String rrmId);

    /**
     * Method for getting all DEH Resources metrics from DB
     *
     * @param pageable - object with defined page, size and sort
     * @return page with all DEH Resources metrics in DB
     */
    Page<Metrics> findAll(Pageable pageable);

    /**
     * Method for getting all owners DEH Resources metrics with daily peaks from DB
     *
     * @return list with all DEH Resources which has metrics
     */
    List<MetricsDto> findAllByOwner();

    /**
     * Method for getting all metrics of DEH Resources which corresponds to specific criteria/filter from DB
     *
     * @param predicate - object with criteria
     * @param pageable  - object with defined page, size and sort
     * @return page with all DEH Resources in DB
     */
    Page<Metrics> findAllByQuery(Predicate predicate, Pageable pageable);


    /**
     * Method for getting all metrics of DEH Resource by RRM UID from DB
     *
     * @param rrmId - DEH resource UID
     * @return All metrics for given DEH Resource
     */
    MetricsDto findOneByRrmIdDeh(String rrmId);

    /**
     * Method for getting all metrics of user's DEH Resources with metrics of DEH Resource which are consumed by user
     *
     * @return list with all metrics owned and consumed by user
     */
    Map<String, Object> findAllMetrics();

    /**
     * Method for getting all metrics of DEH Resources which are consumed by user
     *
     * @return list with all metrics consumed by user
     */
    List<MetricsDataDto> findAllMetricsConsumedByUser();
}
