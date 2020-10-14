package eu.demeterh2020.resourceregistrymanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.logging.Loggable;
import eu.demeterh2020.resourceregistrymanagement.repository.DehRepository;
import eu.demeterh2020.resourceregistrymanagement.util.CompatibilityChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class DehResourceServiceImpl implements DehResourceService {

    @Autowired
    private DehRepository dehRepository;

    @Autowired
    private CompatibilityChecker compatibilityChecker;

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DehResource save(DehResource dehResource) {

        Assert.notNull(dehResource, "DEHResource must not be null!");

        // Check if  DEHresource is compatible
        if (compatibilityChecker.checkCompatibility(dehResource) == true) {
            // Set creating time of DEHResource
            dehResource.setCreateAt(LocalDateTime.now());
            // Store DEHResource in DB
            return dehRepository.save(dehResource);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public DehResource update(String uid, String data) throws IOException {

        Assert.hasText(uid, "DEHResource uid must not be null!");
        Assert.notNull(data, "DEHResource update data must not be null!");

        // Get DEHResource from DB by uid
        DehResource dehResource = dehRepository.findByUid(uid).orElse(null);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        ObjectReader objectReader = objectMapper.readerForUpdating(dehResource);
        DehResource updatedDehResource = objectReader.readValue(data, DehResource.class);
        updatedDehResource.setLastUpdate(LocalDateTime.now());

        return dehRepository.save(updatedDehResource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void deleteByUid(String uid) {

        Assert.hasText(uid, "DEHResource uid must not be null!");

        dehRepository.deleteById(uid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Optional<DehResource> findOneByUid(String uid) {

        Assert.hasText(uid, "DEHResource uid must not be null!");

        Optional<DehResource> dehResource = dehRepository.findByUid(uid);

        return dehResource;
    }

    @Override
    public boolean existByUid(String uid) {

        Assert.hasText(uid, "DEHResource uid must not be null!");

        return dehRepository.existsByUid(uid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<DehResource> findAll(Pageable pageable) {

        return dehRepository.findAll(pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<DehResource> findAllByQuery(Predicate predicate, Pageable pageable) {

        Assert.notNull(pageable, "Paging criteria must not be null!");


        return dehRepository.findAll(predicate, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<DehResource> findAllByQuery(Predicate predicate, Pageable pageable, String localisationDistance) {

        List<DehResource> dehResources = new ArrayList<>();

        dehRepository.findAll(predicate,pageable).iterator().forEachRemaining(
                dehResource -> {
                    if(isResourceCloseToCoords(dehResource, localisationDistance)){
                        dehResources.add(dehResource);
                    }
                }
        );

        Page<DehResource> dehResourcesByCoordinates = new PageImpl<>(dehResources, PageRequest.of(pageable.getPageNumber()
                , pageable.getPageSize()),dehResources.size());
        return dehResourcesByCoordinates;

    }
    /**
     * Computes the distance of two coordinates in meters
     * source of formulas: https://www.movable-type.co.uk/scripts/latlong.html
     * @param lat1 latitude of coord 1 (given in degrees)
     * @param lon1 longitude of coord 1 (given in degrees)
     * @param lat2 latitude of coord 2 (given in degrees)
     * @param lon2 longitude of coord 2 (given in degrees)
     * @return distance of two points (given by coordinates) in meters
     */
    private double returnDistanceBetweenCoords(double lat1, double lon1, double lat2, double lon2)
    {
        final double R = 6371000; // earth radius in meters
        double phi1 = lat1 * Math.PI/180;
        double phi2 = lat2 * Math.PI/180;
        double dPhi  = (lat2-lat1) * Math.PI/180;
        double dLambda = (lon2-lon1) * Math.PI/180;

        double a = Math.sin(dPhi/2) * Math.sin(dPhi/2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(dLambda/2) * Math.sin(dLambda/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c; // in meters
    }

    /**
     * Check if the resource coordinates are close (within certain distance) to coordinates (latitude, longitude)
     * @param resource DEH resource to test
     * @param latitude latitude of coordinate given in [-90.0, 90.0] (i.e. degrees)
     * @param longitude longitude of coordinate given in [-180.0, 180.0] (i.e. degrees)
     * @param distance maximum distance to be considered close (given in meters)
     * @return true if one coordinate from object is within distance meters for coordinates (latitude, longitude)
     */
    private boolean isResourceCloseToCoords(DehResource resource, double latitude, double longitude, double distance)
    {
        if (resource.getLocalisation() == null || resource.getLocalisation().size()<=0) return false; // if it does not exist then it's not close
        // check all coordinates to find if one of them is within the specified distance
        for (int i=0; i<resource.getLocalisation().size(); i++)
        {
            if (returnDistanceBetweenCoords(latitude, longitude, resource.getLocalisation().get(i).getY(), resource.getLocalisation().get(i).getX())<distance)
                return true; // this coordinate is within distance of (latitude, longitude)
        }
        return false; // no coordinate is close!
    }

    /**
     * Check if the resource coordinates are close (within certain distance) to coordinates (latitude, longitude)
     * @param resource resource DEH resource to test
     * @param localisationRequest string of localisation request given in format "latitude,longitude,distance"
     * @return false if localisation if not correctly formatted otherwise checks the distance using the other
     */
    private boolean isResourceCloseToCoords(DehResource resource, String localisationRequest)
    {
        String[] sarr = localisationRequest.split(",");
        if (sarr.length<3) return false; // the input string is given incorrectly
        double latitude=0, longitude=0, distance=0;
        try
        {
            latitude = Double.parseDouble(sarr[0]);
            longitude = Double.parseDouble(sarr[1]);
            distance = Double.parseDouble(sarr[2]);
        }
        catch (NumberFormatException e)
        {
            return false; // localisation request string is mal-formatted...
        }
        //System.out.printf("%f %f %f\n", latitude, longitude, distance);
        return isResourceCloseToCoords(resource, latitude, longitude, distance);
    }
}
