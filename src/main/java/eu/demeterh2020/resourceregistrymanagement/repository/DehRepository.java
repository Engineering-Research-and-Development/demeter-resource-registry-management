package eu.demeterh2020.resourceregistrymanagement.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.domain.QDehResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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

    Set<DehResource> findAll(Predicate predicate);

    Page<DehResource> findAll(Predicate predicate, Pageable pageable);

    Set<DehResource> findAllByAccessibilityAndStatus(int accessibility, int status);

    Set<DehResource> findAllByOwner(String owner);

    @Query(value = "{ '_id' : ?0 }", fields = "{ 'owner' : 1}")
    Optional<DehResource> findOwnerByUid(String uid);

    void deleteByUid(String uid);

    boolean existsByUid(String uid);

    boolean existsByName(String name);

    @Override
    default void customize(QuerydslBindings bindings, QDehResource root) {

        bindings.excluding(root.attachments);
        bindings.excluding(root.images);
        bindings.excluding(root.localisation);
        bindings.excluding(root.billingInformation);
        bindings.excluding(root.downloadsHistory);
        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value));

        bindings.bind(root.description).first((path, value) -> path.containsIgnoreCase(value)
                .or(root.name.containsIgnoreCase(value))
                .or(root.category.any().containsIgnoreCase(value))
                .or(root.tags.any().containsIgnoreCase(value))
                .or(root.dependencies.any().containsIgnoreCase(value)));

        bindings.bind(root.category).all((path, value) -> {
            BooleanBuilder predicate = new BooleanBuilder();
            value.forEach(o -> predicate.or(path.any().containsIgnoreCase(o.iterator().next())));
            return Optional.of(predicate);
        });
        bindings.bind(root.tags).all((path, value) -> {
            BooleanBuilder predicate = new BooleanBuilder();
            value.forEach(o -> predicate.or(path.any().containsIgnoreCase(o.iterator().next())));
            return Optional.of(predicate);
        });
        bindings.bind(root.dependencies).all((path, value) -> {
            BooleanBuilder predicate = new BooleanBuilder();
            value.forEach(o -> predicate.or(path.any().containsIgnoreCase(o.iterator().next())));
            return Optional.of(predicate);
        });
        bindings.bind(root.accessControlPolicies).all((path, value) -> {
            BooleanBuilder predicate = new BooleanBuilder();
            value.forEach(o -> predicate.or(path.any().containsIgnoreCase(o.iterator().next())));
            return Optional.of(predicate);
        });

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
