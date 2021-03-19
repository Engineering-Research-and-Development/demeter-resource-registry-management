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
import eu.demeterh2020.resourceregistrymanagement.domain.Attachment;
import eu.demeterh2020.resourceregistrymanagement.domain.Author;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.domain.QDehResource;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.DehResourceForCreationDTO;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.DehResourceForCreationDtoMultipart;
import eu.demeterh2020.resourceregistrymanagement.logging.Loggable;
import eu.demeterh2020.resourceregistrymanagement.repository.DehRepository;
import eu.demeterh2020.resourceregistrymanagement.security.dto.RrmToken;
import eu.demeterh2020.resourceregistrymanagement.security.dto.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonModule;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class DehResourceServiceImpl implements DehResourceService {

    private final static Logger log = LoggerFactory.getLogger(DehResourceServiceImpl.class);

    @Autowired
    private DehRepository dehRepository;


    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AttachmentService attachmentService;


    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DehResource save(DehResource dehResource) {

        Assert.notNull(dehResource, "DEHResource must not be null!");

        UserInfo authenticatedUser = getAuthenticatedUser();

        log.info("Saving DEHResource.", dehResource);
        dehResource.setOwner(authenticatedUser.getId());
        dehResource.setAuthor(new Author(authenticatedUser.getUsername(), authenticatedUser.getEmail()));

        // Store DEHResource in DB
        return dehRepository.save(dehResource);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DehResource partialUpdate(String uid, JsonPatch patch) throws JsonPatchException, JsonProcessingException {

        Assert.hasText(uid, "DEHResource uid must not be null!");
        Assert.notNull(patch, "DEHResource update data must not be null!");

        log.info("Partial update for DEHResource with uid:" + uid);

        // Get DEHResource from DB by uid
        DehResource targetDehResource = dehRepository.findByUid(uid).orElse(null);

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
    public DehResource update(String uid, DehResourceForCreationDTO dehResourceForUpdating) {

        Assert.hasText(uid, "DEHResource uid must not be null!");
        Assert.notNull(dehResourceForUpdating, "DEHResource update data must not be null!");

        log.info("Update for DEHResource with uid:" + uid);

        // Get DEHResource from DB by uid
        DehResource targetDehResource = dehRepository.findByUid(uid).orElse(null);

        targetDehResource.setName(dehResourceForUpdating.getName());
        targetDehResource.setType(dehResourceForUpdating.getType());
        targetDehResource.setCategory(dehResourceForUpdating.getCategory());
        targetDehResource.setDescription(dehResourceForUpdating.getDescription());
        targetDehResource.setEndpoint(dehResourceForUpdating.getEndpoint());
        targetDehResource.setStatus(dehResourceForUpdating.getStatus());
        targetDehResource.setVersion(dehResourceForUpdating.getVersion());
        targetDehResource.setMaturityLevel(dehResourceForUpdating.getMaturityLevel());
        targetDehResource.setTags(dehResourceForUpdating.getTags());
        targetDehResource.setLocalisation(dehResourceForUpdating.getLocalisation());
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

        return dehRepository.save(targetDehResource);
    }

    //TODO Delete this after testing
    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DehResource updateMultipartForm(String uid, DehResourceForCreationDtoMultipart dehResourceForUpdating) throws IOException {

        Assert.hasText(uid, "DEHResource uid must not be null!");
        Assert.notNull(dehResourceForUpdating, "DEHResource update data must not be null!");

        log.info("Update for DEHResource with uid:" + uid);

        // Get DEHResource from DB by uid
        DehResource targetDehResource = dehRepository.findByUid(uid).orElse(null);


        //Save attachments
        if (dehResourceForUpdating.getAttachmentFile() != null
                && !dehResourceForUpdating.getAttachmentFile().iterator().next().getResource().getFilename().equalsIgnoreCase("")) {
            List<Attachment> savedAttachments = new ArrayList<>();
            List<MultipartFile> attachments = dehResourceForUpdating.getAttachmentFile();
            for (MultipartFile uploadedFile : attachments) {
                String attachmentId = attachmentService.saveAttachment(uploadedFile);
                savedAttachments.add(attachmentService.getAttachment(attachmentId));
            }
            targetDehResource.setAttachment(savedAttachments);
        }

        List<GeoJsonPoint> location = new ArrayList<>();
        location.add(dehResourceForUpdating.getLocalisation());

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

        return dehRepository.save(targetDehResource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void deleteByUid(String uid) {

        Assert.hasText(uid, "DEHResource uid must not be null!");

        log.info("Deleting DEHResource with uid:" + uid);

        dehRepository.deleteByUid(uid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Optional<DehResource> findOneByUid(String uid) {

        Assert.hasText(uid, "DEHResource uid must not be null!");

        log.info("Fetching DEHResource with uid:" + uid);

        Optional<DehResource> dehResource = dehRepository.findByUid(uid);

        return dehResource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existByUid(String uid) {

        log.info("Checking if DEHResource with uid exists:" + uid);

        return dehRepository.existsByUid(uid);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existByName(String name) {

        log.info("Checking if DEHResource with name exists:" + name);

        return dehRepository.existsByName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<DehResource> findAll(Pageable pageable) {


        log.info("Fetching all DEHResources from DB");

        Set<DehResource> allPublicResources = dehRepository.findAllByAccessibilityAndStatus(0, 1);
        Set<DehResource> allOwnersResources = dehRepository.findAllByOwner(getAuthenticatedUser().getId());

        return setPaging(allPublicResources, allOwnersResources, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<DehResource> findAllByQuery(Predicate predicate, Pageable pageable) {

        log.info("Fetching all DEHResources from DB with filters");

        Predicate newPredicate = QDehResource.dehResource.status.eq(1).and(QDehResource.dehResource.accessibility.eq(0)).and(predicate);
        Predicate newPredicate1 = QDehResource.dehResource.owner.eq(getAuthenticatedUser().getId()).and(predicate);
        Assert.notNull(pageable, "Paging criteria must not be null!");
        Set<DehResource> allPublicResources = dehRepository.findAll(newPredicate, pageable).stream().collect(Collectors.toSet());
        Set<DehResource> allOwnersResources = dehRepository.findAll(newPredicate1, pageable).stream().collect(Collectors.toSet());


        return setPaging(allPublicResources, allOwnersResources, pageable);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<DehResource> findAllByQuery(Predicate predicate, Pageable pageable, String localisationDistance) {

        log.info("Fetching all DEHResources from DB with filters and distance");

        List<DehResource> dehResources = new ArrayList<>();

        Predicate newPredicate = QDehResource.dehResource.status.eq(1).and(QDehResource.dehResource.accessibility.eq(0)).and(predicate);
        Predicate newPredicate1 = QDehResource.dehResource.owner.eq(getAuthenticatedUser().getId()).and(predicate);
        Assert.notNull(pageable, "Paging criteria must not be null!");
        Set<DehResource> allPublicResources = dehRepository.findAll(newPredicate, pageable).stream().collect(Collectors.toSet());
        Set<DehResource> allOwnersResources = dehRepository.findAll(newPredicate1, pageable).stream().collect(Collectors.toSet());

        allPublicResources.addAll(allOwnersResources);
        allPublicResources.iterator().forEachRemaining(
                dehResource -> {
                    if (isResourceCloseToCoords(dehResource, localisationDistance)) {
                        dehResources.add(dehResource);
                    }
                }
        );

        Page<DehResource> dehResourcesByCoordinates = new PageImpl<>(dehResources, PageRequest.of(pageable.getPageNumber()
                , pageable.getPageSize()), dehResources.size());

        return dehResourcesByCoordinates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public List<String> findAllCategories() {

        log.info("Fetching all DEHResources categories");

        List<String> categories = mongoTemplate.query(DehResource.class).distinct("category").as(String.class).all();

        return categories;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> findAllTypes() {

        log.info("Fetching all DEHResources types");

        List<String> types = mongoTemplate.query(DehResource.class).distinct("type").as(String.class).all();

        return types;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DehResource updateNumberOfDownloads(String resourceUid) {

        Assert.hasText(resourceUid, "DEHResource uid must not be null!");

        log.info("Updating number of downloads for DEHResource with uid: " + resourceUid);

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
        if (resource.getLocalisation() == null || resource.getLocalisation().size() <= 0)
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
        String[] sarr = localisationRequest.split(",");
        if (sarr.length < 3) return false; // the input string is given incorrectly
        double latitude = 0, longitude = 0, distance = 0;
        try {
            latitude = Double.parseDouble(sarr[0]);
            longitude = Double.parseDouble(sarr[1]);
            distance = Double.parseDouble(sarr[2]);
        } catch (NumberFormatException e) {
            return false; // localisation request string is mal-formatted...
        }

        return isResourceCloseToCoords(resource, latitude, longitude, distance);
    }

    private Page<DehResource> setPaging(Set<DehResource> allPublicResources, Set<DehResource> allOwnerResources, Pageable pageable) {

        allPublicResources.addAll(allOwnerResources);

        List<DehResource> dehResources = new ArrayList<>();
        dehResources.addAll(allPublicResources);

        Page<DehResource> dehResourcePage = new PageImpl<>(dehResources, PageRequest.of(pageable.getPageNumber()
                , pageable.getPageSize()), dehResources.size());

        return dehResourcePage;
    }

    private UserInfo getAuthenticatedUser() {
        RrmToken authenticatedRrmToken = (RrmToken) SecurityContextHolder.getContext().getAuthentication();
        UserInfo authenticatedUserInfo = authenticatedRrmToken.getUserInfo();

        return authenticatedUserInfo;

    }
}
