package eu.demeterh2020.resourceregistrymanagement.repository;

import eu.demeterh2020.resourceregistrymanagement.domain.Audit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuditRepository extends MongoRepository<Audit, String> {

    Optional<Audit> findByUid(String uid);

    void deleteByUid(String resourceUid);
}
