package eu.demeterh2020.resourceregistrymanagement.repository;

import eu.demeterh2020.resourceregistrymanagement.domain.Metrics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MetricsRepository extends MongoRepository<Metrics, String> {

    Optional<Metrics> findByRrmId(String rrmId);

    boolean existsByRrmId(String rrmId);

    List<Metrics> findAllByOwner(String owner);

    @Query(value = "{'containers.containerId' : ?0}")
    Optional<Metrics> findByContainerId(String containerId);

    @Query(value = "{'containers.consumerId' : ?0}")
    Optional<List<Metrics>> findByConsumerId(String containerId);

    void deleteByRrmId(String rrmId);
}
