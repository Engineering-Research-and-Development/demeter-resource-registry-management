package eu.demeterh2020.resourceregistrymanagement.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.domain.QDehResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DehRepository extends MongoRepository<DehResource, String>, QuerydslPredicateExecutor<DehResource>, QuerydslBinderCustomizer<QDehResource> {

    Optional<DehResource> findByUid(String uid);

    Page<DehResource> findAll(Pageable page);

    Page<DehResource> findAll(Predicate predicate, Pageable pageable);

    Set<DehResource> findAllByStatus(int status);

    Set<DehResource> findAllByAccessibility(int accessibility);

    Set<DehResource> findAllByAccessibilityAndStatus(int accessibility, int status);

    Set<DehResource> findAllByOwner(String owner);

    void deleteByUid(String uid);

    boolean existsByUid(String uid);

    boolean existsByName(String name);

    @Override
    default void customize(QuerydslBindings bindings, QDehResource root) {

        bindings.excluding(root.attachment);
        bindings.excluding(root.localisation);
        bindings.excluding(root.billingInformation);
        bindings.excluding(root.downloadsHistory);
        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value));
        bindings.bind(root.category).first((path, value) -> path.any().containsIgnoreCase(value.iterator().next()));
        bindings.bind(root.tags).first((path, value) -> path.any().containsIgnoreCase(value.iterator().next()));
        bindings.bind(root.dependencies).first((path, value) -> path.any().containsIgnoreCase(value.iterator().next()));
        bindings.bind(root.accessControlPolicies).first((path, value) -> path.any().containsIgnoreCase(value.iterator().next()));

        bindings.bind(root.rating).all((path, value) -> {
            Iterator<? extends Double> it = value.iterator();
            Double from = it.next();
            if (value.size() >= 2) {
                Double to = it.next();
                return Optional.of(path.between(from, to));
            } else {
                return Optional.of(path.goe(from));
            }
        });

        bindings.bind(root.maturityLevel).all((path, value) -> {
            Iterator<? extends Integer> it = value.iterator();
            Integer from = it.next();
            if (value.size() >= 2) {
                Integer to = it.next();
                return Optional.of(path.between(from, to));
            } else {
                return Optional.of(path.goe(from));
            }
        });

        bindings.bind(root.createAt).all((path, value) -> {
            Iterator<? extends LocalDateTime> it = value.iterator();
            LocalDateTime from = it.next();
            if (value.size() >= 2) {
                LocalDateTime to = it.next();
                return Optional.of(path.between(from, to));
            } else {
                return Optional.of(path.goe(from));
            }
        });

        bindings.bind(root.lastUpdate).all((path, value) -> {
            Iterator<? extends LocalDateTime> it = value.iterator();
            LocalDateTime from = it.next();
            if (value.size() >= 2) {
                LocalDateTime to = it.next();
                return Optional.of(path.between(from, to));
            } else {
                return Optional.of(path.goe(from));
            }
        });

    }
}
