package eu.demeterh2020.resourceregistrymanagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.Author;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.domain.QDehResource;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.DehResourceForUpdateDto;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceAlreadyExists;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceNotFoundException;
import eu.demeterh2020.resourceregistrymanagement.exception.UnauthorizedException;
import eu.demeterh2020.resourceregistrymanagement.logging.Loggable;
import eu.demeterh2020.resourceregistrymanagement.repository.DehRepository;
import eu.demeterh2020.resourceregistrymanagement.security.dto.RrmToken;
import eu.demeterh2020.resourceregistrymanagement.security.dto.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonModule;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class DehResourceServiceImpl implements DehResourceService {

    private final static Logger log = LoggerFactory.getLogger(DehResourceServiceImpl.class);

    @Autowired
    private DehRepository dehRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    AuditService auditService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DehResource save(DehResource dehResource) {

        log.info("Method save() called.");
        log.info("Saving DEHResource.", dehResource.toString());

        Assert.notNull(dehResource, "DehResource must not be null!");

        if (dehRepository.existsByName(dehResource.getName())) {
            throw new ResourceAlreadyExists("Resource with a name " + dehResource.getName() + " already exists");
        }

        dehResource.setOwner(getAuthenticatedUser().getId());
        dehResource.setAuthor(new Author(getAuthenticatedUser().getUsername(), getAuthenticatedUser().getEmail()));

        // Store DEHResource in DB
        return dehRepository.save(dehResource);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DehResource partialUpdate(String uid, JsonPatch patch) throws JsonPatchException, JsonProcessingException {

        log.info("Method partialUpdate() called.");
        log.info("Partial update for DEHResource with uid:" + uid);

        Assert.hasText(uid, "DEHResource uid must not be null!");
        Assert.notNull(patch, "DEHResource update data must not be null!");


        // Get DEHResource from DB by uid
        DehResource targetDehResource = dehRepository.findByUid(uid).orElse(null);


        if (!targetDehResource.getOwner().equals(getAuthenticatedUser().getId())) {
            log.error("Access denied for resource with uid:" + uid + "for user: " + getAuthenticatedUser().getId());
            throw new UnauthorizedException("Access denied for resource with uid: " + uid);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new GeoJsonModule());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JsonNode patched = patch.apply(objectMapper.convertValue(targetDehResource, JsonNode.class));

        DehResource dehResourcePatched = objectMapper.treeToValue(patched, DehResource.class);

        return dehRepository.save(dehResourcePatched);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DehResource update(String uid, DehResourceForUpdateDto dehResourceForUpdating) throws IOException {

        log.info("Method update() called. Multipart file");
        log.info("Update for DEHResource with uid:" + uid);

        Assert.hasText(uid, "DEHResource uid must not be null!");
        Assert.notNull(dehResourceForUpdating, "DEHResource update data must not be null!");


        // Get DEHResource from DB by uid
        DehResource targetDehResource = dehRepository.findByUid(uid).orElse(null);

        if (!targetDehResource.getOwner().equals(getAuthenticatedUser().getId())) {
            log.error("Access denied for resource with uid:" + uid + "for user: " + getAuthenticatedUser().getId());
            throw new UnauthorizedException("Access denied for resource with uid: " + uid);
        }

        if (dehRepository.existsByName(dehResourceForUpdating.getName()) && !targetDehResource.getOwner().equals(getAuthenticatedUser().getId())) {
            if (!targetDehResource.getOwner().equals(getAuthenticatedUser().getId())) {
                throw new ResourceAlreadyExists("Resource with the name " + dehResourceForUpdating.getName() + " already exists, please choose other name.");
            }
            if (targetDehResource.getOwner().equals(getAuthenticatedUser().getId()) && !targetDehResource.getUid().equals(uid)) {
                throw new ResourceAlreadyExists("You already have DEH resource with the name" + dehResourceForUpdating.getName() + ", please choose other name.");
            }
        }

        if (dehResourceForUpdating.getDeleteFiles().size() > 0) {
            for (String deleteFile : dehResourceForUpdating.getDeleteFiles()) {
                attachmentService.deleteAttachmentById(deleteFile);
                targetDehResource.getAttachments().removeIf(attachment -> attachment.getId().equals(deleteFile));
                targetDehResource.getImages().removeIf(image -> image.getId().equals(deleteFile));

            }
        }

        if ((dehResourceForUpdating.getImages() != null
                && !(dehResourceForUpdating.getImages().iterator().next().getResource().getFilename().equalsIgnoreCase("")))
                || (dehResourceForUpdating.getAttachments() != null
                && !(dehResourceForUpdating.getAttachments().iterator().next().getResource().getFilename().equalsIgnoreCase("")))) {

            if (dehResourceForUpdating.getImages() != null
                    && !(dehResourceForUpdating.getImages().iterator().next().getResource().getFilename().equalsIgnoreCase(""))) {
                log.info("IMAGES EXISTS IN DTO");
                targetDehResource.getImages().addAll(attachmentService.saveMultipleAttachments(dehResourceForUpdating.getImages()));
            }

            if (dehResourceForUpdating.getAttachments() != null
                    && !(dehResourceForUpdating.getAttachments().iterator().next().getResource().getFilename().equalsIgnoreCase(""))) {
                log.info("ATTACHMENTS EXISTS IN DTO");

                targetDehResource.getAttachments().addAll(attachmentService.saveMultipleAttachments(dehResourceForUpdating.getAttachments()));
            }

        }
        List<GeoJsonPoint> location = new ArrayList<>();

        GeoJsonPoint updatedLocation = null;

        if (dehResourceForUpdating.getLocalisation() != null) {
            updatedLocation = new GeoJsonPoint(dehResourceForUpdating.getLocalisation().getY(), dehResourceForUpdating.getLocalisation().getX());
        }

        location.add(updatedLocation);

        targetDehResource.setName(dehResourceForUpdating.getName());
        targetDehResource.setType(dehResourceForUpdating.getType());
        targetDehResource.setCategory(dehResourceForUpdating.getCategory());
        targetDehResource.setDescription(dehResourceForUpdating.getDescription());
        targetDehResource.setEndpoint(dehResourceForUpdating.getEndpoint());
        targetDehResource.setStatus(dehResourceForUpdating.getStatus());
        targetDehResource.setVersion(dehResourceForUpdating.getVersion());
        targetDehResource.setMaturityLevel(dehResourceForUpdating.getMaturityLevel());
        targetDehResource.setTags(dehResourceForUpdating.getTags());
        targetDehResource.setLocalisation(location);
        targetDehResource.setAccessibility(dehResourceForUpdating.getAccessibility());
        targetDehResource.setDependencies(dehResourceForUpdating.getDependencies());
        targetDehResource.setAccessControlPolicies(dehResourceForUpdating.getAccessControlPolicies());
        targetDehResource.setUrl(dehResourceForUpdating.getUrl());
        targetDehResource.setLastUpdate(LocalDateTime.now());
        targetDehResource.setAuthor(new Author(getAuthenticatedUser().getUsername(), getAuthenticatedUser().getEmail()));
        //TODO Fix the implementation
//        ModelMapper modelMapper = new ModelMapper();
//        modelMapper.getConfiguration().setSkipNullEnabled(true);
//        modelMapper.map(dehResourceForUpdating, targetDehResource);

        log.info("DEH RESOURCE BEFORE STORING IN DB", targetDehResource);
        return dehRepository.save(targetDehResource);
    }

    @Override
    public String findOwnerByUid(String uid) {

        Optional<DehResource> dehResource = dehRepository.findOwnerByUid(uid);

        if (!dehResource.isPresent()) {
            log.error("Resource with uid:" + uid + " not found");
            throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
        }
        return dehResource.get().getOwner();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void deleteByUid(String uid) {

        log.info("Method deleteByUid() called.");
        log.info("Deleting DEHResource with uid:" + uid);

        Assert.hasText(uid, "DEHResource uid must not be null!");

        DehResource targetDehResource = dehRepository.findByUid(uid).orElse(null);

        if (!targetDehResource.getOwner().equals(getAuthenticatedUser().getId())) {
            log.error("Access denied for resource with uid:" + uid + "for user: " + getAuthenticatedUser().getId());
            throw new UnauthorizedException("Access denied for resource with uid: " + uid);
        }

        attachmentService.deleteAttachments(dehRepository.findByUid(uid).get().getAttachments());
        attachmentService.deleteAttachments(dehRepository.findByUid(uid).get().getImages());
        metricsService.deleteByRrmId(uid);
        dehRepository.deleteByUid(uid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Optional<DehResource> findOneByUid(String uid) {

        log.info("Method findByUid() called.");
        log.info("Fetching DEHResource with uid:" + uid);

        Assert.hasText(uid, "DEHResource uid must not be null!");

        Optional<DehResource> dehResource = dehRepository.findByUid(uid);

        //Check if resource exist in DB
        if (!dehResource.isPresent()) {
            log.error("Resource with uid:" + uid + " not found");
            throw new ResourceNotFoundException("Resource with uid:" + uid + " not found");
        }
        //Check if resource is public
        if ((dehResource.get().getAccessibility() == 0) && (dehResource.get().getStatus() == 1)) {
            log.info("DEH Resource with uid:" + uid + " exist in DB.");
            return dehResource;
        }
        //Check if authenticated user is owner of resoruce
        else if (dehResource.get().getOwner().equals(getAuthenticatedUser().getId())) {
            return dehResource;
        }

        log.error("Access denied for resource with uid:" + uid + "for user: " + getAuthenticatedUser().getId());
        throw new UnauthorizedException("Access denied for resource with uid: " + uid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public boolean existByUid(String uid) {

        log.info("Method existByUid() called.");
        log.info("Checking if DEHResource with uid exists:" + uid);

        return dehRepository.existsByUid(uid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public boolean existByName(String name) {

        log.info("Method existByName() called.");
        log.info("Checking if DEHResource with name exists:" + name);

        return dehRepository.existsByName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<DehResource> findAll(Pageable pageable) {

        log.info("Method findAll() called.");
        log.info("Fetching all DEHResources from DB");

        Set<DehResource> allPublicResources = dehRepository.findAllByAccessibilityAndStatus(0, 1);
        Set<DehResource> allOwnersResources = dehRepository.findAllByOwner(getAuthenticatedUser().getId());
        allPublicResources.addAll(allOwnersResources);

        return createPage(allPublicResources, null, pageable);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<DehResource> findAllByQuery(Predicate predicate, Pageable pageable, String localisationDistance) {

        log.info("Method findAllByQuery() with distance called.");
        log.info("Fetching all DEHResources from DB with filters and distance");

        Assert.notNull(pageable, "Paging criteria must not be null!");

        List<DehResource> dehResources = new ArrayList<>();

        Predicate newPredicate = QDehResource.dehResource.status.eq(1).and(QDehResource.dehResource.accessibility.eq(0)).and(predicate);
        Predicate newPredicate1 = QDehResource.dehResource.owner.eq(getAuthenticatedUser().getId()).and(predicate);
        Set<DehResource> allPublicResources = dehRepository.findAll(newPredicate);
        Set<DehResource> allOwnersResources = dehRepository.findAll(newPredicate1);
        allPublicResources.addAll(allOwnersResources);

        if (localisationDistance != null) {
            allPublicResources.iterator().forEachRemaining(
                    dehResource -> {
                        if (isResourceCloseToCoords(dehResource, localisationDistance)) {
                            dehResources.add(dehResource);
                        }
                    }
            );

            return createPage(null, dehResources, pageable);
        }

        return createPage(allPublicResources, null, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public List<String> findAllCategories() {

        log.info("Method findAllCategories() called.");
        log.info("Fetching all DEHResources categories");

        List<String> categories = mongoTemplate.query(DehResource.class).distinct("category").as(String.class).all();

        return categories;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public List<String> findAllTypes() {

        log.info("Method findAllTypes() called.");
        log.info("Fetching all DEHResources types");

        List<String> types = mongoTemplate.query(DehResource.class).distinct("type").as(String.class).all();

        return types;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DehResource updateNumberOfDownloads(String resourceUid) {

        log.info("Method updateNumberOfDownloads() called.");
        log.info("Updating number of downloads for DEHResource with uid: " + resourceUid);

        Assert.hasText(resourceUid, "DEHResource uid must not be null!");

        Optional<DehResource> resourceAudit = dehRepository.findByUid(resourceUid);

        Map<LocalDate, Integer> numberOfDownloads = resourceAudit.get().getDownloadsHistory();
        if (resourceAudit.isPresent()) {
            if (numberOfDownloads.containsKey(LocalDate.now())) {
                numberOfDownloads.put(LocalDate.now(), numberOfDownloads.get(LocalDate.now()) + 1);
            } else {
                numberOfDownloads.put(LocalDate.now(), 1);
            }
        }
        return dehRepository.save(resourceAudit.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DehResource rateResource(String uid, Double rating) {

        log.info("Method rateResource() called.");
        log.info("Updating rating for DEHResource with uid: " + uid);

        Assert.hasText(uid, "DEHResource uid must not be null!");

        Optional<DehResource> dehResource = dehRepository.findByUid(uid);

        Double updatedRating = auditService.updateRatingByUid(uid, rating);

        dehResource.get().setRating(updatedRating);

        return dehRepository.save(dehResource.get());
    }

    /**
     * Computes the distance of two coordinates in meters
     * source of formulas: https://www.movable-type.co.uk/scripts/latlong.html
     *
     * @param lat1 latitude of coord 1 (given in degrees)
     * @param lon1 longitude of coord 1 (given in degrees)
     * @param lat2 latitude of coord 2 (given in degrees)
     * @param lon2 longitude of coord 2 (given in degrees)
     * @return distance of two points (given by coordinates) in meters
     */
    private double returnDistanceBetweenCoords(double lat1, double lon1, double lat2, double lon2) {

        log.info("Method returnDistanceBetweenCoords() called.");

        final double R = 6371e3; // earth radius in meters
        double phi1 = lat1 * Math.PI / 180;
        double phi2 = lat2 * Math.PI / 180;
        double dPhi = (lat2 - lat1) * Math.PI / 180;
        double dLambda = (lon2 - lon1) * Math.PI / 180;
        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // in meters
    }

    /**
     * Check if the resource coordinates are close (within certain distance) to coordinates (latitude, longitude)
     *
     * @param resource  DEH resource to test
     * @param latitude  latitude of coordinate given in [-90.0, 90.0] (i.e. degrees)
     * @param longitude longitude of coordinate given in [-180.0, 180.0] (i.e. degrees)
     * @param distance  maximum distance to be considered close (given in meters)
     * @return true if one coordinate from object is within distance meters for coordinates (latitude, longitude)
     */
    private boolean isResourceCloseToCoords(DehResource resource, double latitude, double longitude, double distance) {

        log.info("Method isResourceCloseToCoords() called.");

        if (resource.getLocalisation() == null || resource.getLocalisation().size() <= 0 || resource.getLocalisation().get(0) == null)
            return false; // if it does not exist then it's not close
        // check all coordinates to find if one of them is within the specified distance
        for (int i = 0; i < resource.getLocalisation().size(); i++) {
            if (returnDistanceBetweenCoords(latitude, longitude, resource.getLocalisation().get(i).getX(), resource.getLocalisation().get(i).getY()) < distance)
                return true; // this coordinate is within distance of (latitude, longitude)
        }

        return false; // no coordinate is close!
    }

    /**
     * Check if the resource coordinates are close (within certain distance) to coordinates (latitude, longitude)
     *
     * @param resource            resource DEH resource to test
     * @param localisationRequest string of localisation request given in format "latitude,longitude,distance"
     * @return false if localisation if not correctly formatted otherwise checks the distance using the other
     */
    private boolean isResourceCloseToCoords(DehResource resource, String localisationRequest) {

        log.info("Method isResourceCloseToCoords() called.");

        String[] sarr = localisationRequest.split(",");
        if (sarr.length < 3) return false; // the input string is given incorrectly
        double latitude = 0, longitude = 0, distance = 0;

        try {
            latitude = Double.parseDouble(sarr[0]);
            longitude = Double.parseDouble(sarr[1]);
            distance = Double.parseDouble(sarr[2]) * 1000;
        } catch (NumberFormatException e) {
            return false; // localisation request string is mal-formatted...
        }

        return isResourceCloseToCoords(resource, latitude, longitude, distance);
    }

    /**
     * Get information about authenticated user
     *
     * @return information about user, such as id, email, etc
     */
    private UserInfo getAuthenticatedUser() {

        log.info("Method getAuthenticatedUser() called.");

        RrmToken authenticatedRrmToken = (RrmToken) SecurityContextHolder.getContext().getAuthentication();
        UserInfo authenticatedUserInfo = authenticatedRrmToken.getUserInfo();

        return authenticatedUserInfo;
    }

    /**
     * Get information about authenticated user
     *
     * @return information about user, such as id, email, etc
     */
    private Page<DehResource> createPage(Set dehResourcesSet, List<DehResource> dehResourceList, Pageable pageable) {

        int start;
        int end;

        if (dehResourceList != null) {
            start = (int) pageable.getOffset();
            end = Math.min((start + pageable.getPageSize()), dehResourceList.size());
            if (start <= end) {
                return new PageImpl<>(dehResourceList.subList(start, end), pageable, dehResourceList.size());
            }
            return new PageImpl<>(dehResourceList);
        }

        List<DehResource> allResources = new ArrayList<>();
        allResources.addAll(dehResourcesSet);
        start = (int) pageable.getOffset();
        end = Math.min((start + pageable.getPageSize()), allResources.size());

        if (start <= end) {
            return new PageImpl<>(allResources.subList(start, end), pageable, allResources.size());
        }

        return new PageImpl<>(allResources);
    }
}
