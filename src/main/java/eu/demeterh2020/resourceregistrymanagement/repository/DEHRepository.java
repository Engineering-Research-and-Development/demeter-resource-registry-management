package eu.demeterh2020.resourceregistrymanagement.repository;

import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;
import eu.demeterh2020.resourceregistrymanagement.domain.QDEHResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DEHRepository extends MongoRepository<DEHResource, String>, QuerydslPredicateExecutor<DEHResource>, QuerydslBinderCustomizer<QDEHResource> {

    Optional<DEHResource> findByUid(String uid);

    Page<DEHResource> findAll(Pageable page);

    Page<DEHResource> findAll(Predicate predicate, Pageable pageable);

    void deleteByUid(String uid);

    boolean existsByUid(String uid);


    @Override
    default void customize(QuerydslBindings bindings, QDEHResource root) {
        //TODO Refactor and add binding for ListPath<String, StringPath>
        bindings.bind(root.uid).first((path, value) -> path.containsIgnoreCase(value));
        bindings.bind(root.name).first((path, value) -> path.containsIgnoreCase(value));
        bindings.bind(root.type).first((path, value) -> path.containsIgnoreCase(value));
        bindings.bind(root.description).first((path, value) -> path.containsIgnoreCase(value));
        bindings.bind(root.endpoint).first((path, value) -> path.containsIgnoreCase(value));
        bindings.bind(root.status).first((path, value) -> path.containsIgnoreCase(value));
        bindings.bind(root.version).first((path, value) -> path.containsIgnoreCase(value));
        bindings.bind(root.owner).first((path, value) -> path.containsIgnoreCase(value));
        bindings.bind(root.url).first((path, value) -> path.containsIgnoreCase(value));
    }
}
