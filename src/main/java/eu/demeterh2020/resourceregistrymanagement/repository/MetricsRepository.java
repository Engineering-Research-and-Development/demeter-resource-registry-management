package eu.demeterh2020.resourceregistrymanagement.repository;

import eu.demeterh2020.resourceregistrymanagement.domain.Metrics;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.UserResourceMetricsDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MetricsRepository extends MongoRepository<Metrics, String> {

    Optional<Metrics> findByRrmId(String rrmId);

    boolean existsByRrmId(String rrmId);

    @Query(value = "{ 'owner' : ?0 }", fields = "{ 'rrmId' : 1, 'name' : 1, 'numberOfInstances' : 1}")
    List<UserResourceMetricsDto> findAllByOwner(String owner);

    @Query(value = "{'containers.containerId' : ?0}")
    Optional<Metrics> findByContainerId(String containerId);

    @Query(value = "{'containers.consumerId' : ?0}")
    Optional<List<Metrics>> findByConsumerId(String containerId);

    void deleteByRrmId(String rrmId);
}
