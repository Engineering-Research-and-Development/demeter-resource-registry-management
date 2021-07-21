package eu.demeterh2020.resourceregistrymanagement.service;

import com.querydsl.core.types.Predicate;
import eu.demeterh2020.resourceregistrymanagement.domain.*;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.MetricsDataDto;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.MetricsDto;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.UserResourceMetricsDto;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceNotFoundException;
import eu.demeterh2020.resourceregistrymanagement.logging.Loggable;
import eu.demeterh2020.resourceregistrymanagement.repository.MetricsRepository;
import eu.demeterh2020.resourceregistrymanagement.security.dto.RrmToken;
import eu.demeterh2020.resourceregistrymanagement.security.dto.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetricsServiceImpl implements MetricsService {

    private final static Logger log = LoggerFactory.getLogger(MetricsServiceImpl.class);

    @Autowired
    MetricsRepository metricsRepository;

    @Autowired
    DehResourceService dehResourceService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void save(List<MetricsDataDto> metrics) {

        log.info("Method save() called.");

        metrics.forEach(metricData -> {
            if (metricsRepository.existsByRrmId(metricData.getRrmId())) {
                log.info("Metrics for DEH Resource with uid: " + metricData.getRrmId() + "exists.");
                Metrics updatedMetrics = updateExistingMetrics(metricData);
                metricsRepository.save(updatedMetrics);
            } else {
                log.info("Metrics for DEH Resource with uid: " + metricData.getRrmId() + " doesn't exists.");
                Metrics newMetric = createNewMetrics(metricData);
                metricsRepository.save(newMetric);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void deleteByRrmId(String rrmId) {
        metricsRepository.deleteByRrmId(rrmId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public MetricsDataDto findOneByContainerId(String containerId) {

        Optional<Metrics> metricsData = metricsRepository.findByContainerId(containerId);

        if (!metricsData.isPresent()) {
            log.error("Metrics data for container with id:" + containerId + " not found");
            throw new ResourceNotFoundException("Metrics data for container with id:" + containerId + " not found");
        }

        return convertFromMetricsDataToMetricsDataDto(metricsData.get().getContainers().stream().filter(container -> container.getContainerId().equals(containerId)).findAny().get());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Metrics findOneByRrmId(String dehResourceUid) {

        Optional<Metrics> metricsData = metricsRepository.findByRrmId(dehResourceUid);

        if (!metricsData.isPresent()) {
            log.error("Metrics data for DEH Resource with uid: " + dehResourceUid + " not found");
            throw new ResourceNotFoundException("Metrics data for DEH Resource with uid: " + dehResourceUid + " not found");
        }

        return metricsData.get();
    }

    @Override
    @Loggable
    public MetricsDto findOneByRrmIdDeh(String dehResourceUid) {

        Optional<Metrics> metricsData = metricsRepository.findByRrmId(dehResourceUid);

        if (!metricsData.isPresent()) {
            log.error("Metrics data for DEH Resource with uid: " + dehResourceUid + " not found");
            throw new ResourceNotFoundException("Metrics data for DEH Resource with uid: " + dehResourceUid + " not found");
        }

        MetricsDto metricsDto = new MetricsDto();
        metricsDto.setRrmId(metricsData.get().getRrmId());
        metricsDto.setName(metricsData.get().getName());
        metricsDto.setNumberOfInstances(metricsData.get().getNumberOfInstances());
        List<MetricsDataDto> containers = new ArrayList<>();


        metricsData.get().getContainers().forEach(container -> containers.add(convertFromMetricsDataToMetricsDataDtoOnlyPeaks(container)));

        metricsDto.setContainers(containers);
        return metricsDto;
    }

    @Override
    @Loggable
    public Map<String, Object> findAllMetrics() {

        Map<String, Object> allMetrics = new HashMap<>();
        List<UserResourceMetricsDto> allOwnersMetrics = findAllByOwner();
        List<MetricsDataDto> allMetricsConsumedByUser = findAllMetricsConsumedByUser();
        allMetrics.put("owned", allOwnersMetrics);
        allMetrics.put("consumed", allMetricsConsumedByUser);

        return allMetrics;
    }

    @Override
    @Loggable
    public List<MetricsDataDto> findAllMetricsConsumedByUser() {
        Optional<List<Metrics>> metricsData = metricsRepository.findByConsumerId(getAuthenticatedUser().getId());
        List<MetricsDataDto> consumerMetricsData = new ArrayList<>();
        if (metricsData.isPresent()) {
            metricsData.get().forEach(metricsDataEntity -> metricsDataEntity.getContainers().stream().filter(container -> container.getConsumerId().equals(getAuthenticatedUser().getId())).collect(Collectors.toList()).forEach(metrics -> {
                if (!metrics.getConsumerId().equalsIgnoreCase(metricsDataEntity.getOwner())) {
                    consumerMetricsData.add(convertFromMetricsDataToMetricsDataDtoOnlyPeaks(metrics));
                }
            }));
        }

        return consumerMetricsData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<Metrics> findAll(Pageable pageable) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public List<UserResourceMetricsDto> findAllByOwner() {

        List<UserResourceMetricsDto> metrics = metricsRepository.findAllByOwner(getAuthenticatedUser().getId());

        return metrics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Page<Metrics> findAllByQuery(Predicate predicate, Pageable pageable) {
        return null;
    }

    private Metrics createNewMetrics(MetricsDataDto metricsData) {

        DehResource dehResource = dehResourceService.findOneByUid(metricsData.getRrmId()).get();
        Metrics metrics = new Metrics();
        List<MetricsData> containers = new ArrayList<>();
        containers.add(convertFromMetricsDataDtoToMetricsData(metricsData));
        metrics.setName(dehResource.getName());
        metrics.setRrmId(metricsData.getRrmId());
        metrics.setOwner(dehResource.getOwner());
        metrics.setContainers(containers);
        metrics.setNumberOfInstances(1);

        return metrics;

    }

    private Metrics updateExistingMetrics(MetricsDataDto metricsData) {

        log.info("Updating metrics for DEH Resource with uid: " + metricsData.getRrmId() + "exists.");

        Metrics existingMetrics = metricsRepository.findByRrmId(metricsData.getRrmId()).get();


        if (!existingMetrics.getContainers().stream().filter(container -> container.getContainerId().equals(metricsData.getContainerId())).findAny().isPresent()) {
            log.info("Creating new container with id:" + metricsData.getContainerId() + " for DEH Resource with uid:" + metricsData.getRrmId());
            existingMetrics.getContainers().add(convertFromMetricsDataDtoToMetricsData(metricsData));
            existingMetrics.setNumberOfInstances(existingMetrics.getNumberOfInstances() + 1);
        } else {
            existingMetrics.getContainers().stream().filter(container -> container.getContainerId().equals(metricsData.getContainerId())).findAny().ifPresent(container -> {
                container.setUptime(metricsData.getUptime());
                container.setLastUpdated(metricsData.getLastUpdated());
                if (!getAuthenticatedUser().getUsername().equalsIgnoreCase(container.getConsumer().getUsername())) {
                    container.getConsumer().setUsername(getAuthenticatedUser().getUsername());
                }

                Map<LocalDate, List<MetricsVolume>> cpuConsumption = distinctConsumptionByDate(metricsData.getCpuConsumption());
                Map<LocalDate, List<MetricsVolume>> memoryConsumption = distinctConsumptionByDate(metricsData.getMemoryConsumption());

                cpuConsumption.forEach((cpuConsumptionDate, cpuConsumptionMetrics) -> {
                    if (container.getCpuConsumption().containsKey(cpuConsumptionDate)) {
                        container.getCpuConsumption().get(cpuConsumptionDate).addAll(cpuConsumptionMetrics);
                        container.getCpuConsumption().get(cpuConsumptionDate).sort(Comparator.comparingDouble(MetricsVolume::getPercent).reversed());
                    } else {
                        container.getCpuConsumption().put(cpuConsumptionDate, cpuConsumptionMetrics);
                    }
                });

                memoryConsumption.forEach((memoryConsumptionDate, memoryConsumptionMetrics) -> {
                    if (container.getMemoryConsumption().containsKey(memoryConsumptionDate)) {
                        container.getMemoryConsumption().get(memoryConsumptionDate).addAll(memoryConsumptionMetrics);
                        container.getMemoryConsumption().get(memoryConsumptionDate).sort(Comparator.comparingDouble(MetricsVolume::getPercent).reversed());
                    } else {
                        container.getMemoryConsumption().put(memoryConsumptionDate, memoryConsumptionMetrics);
                    }
                });
            });

        }

        return existingMetrics;
    }

    private MetricsData convertFromMetricsDataDtoToMetricsData(MetricsDataDto metricsDataDto) {

        MetricsData metrics = new MetricsData();
        metrics.setUptime(metricsDataDto.getUptime());
        metrics.setHostname(metricsDataDto.getHostname());
        metrics.setIp(metricsDataDto.getIp());
        metrics.setImage(metricsDataDto.getImage());
        metrics.setBseId(metricsDataDto.getBseId());
        metrics.setRrmId(metricsDataDto.getRrmId());
        metrics.setLastUpdated(metricsDataDto.getLastUpdated());
        metrics.setContainerId(metricsDataDto.getContainerId());
        metrics.setConsumerId(getAuthenticatedUser().getId());
        metrics.setConsumer(new Author(getAuthenticatedUser().getUsername(), getAuthenticatedUser().getEmail()));

        metrics.setCpuConsumption(distinctConsumptionByDate(metricsDataDto.getCpuConsumption()));
        metrics.setMemoryConsumption(distinctConsumptionByDate(metricsDataDto.getMemoryConsumption()));

        return metrics;
    }

    private Map<LocalDate, List<MetricsVolume>> distinctConsumptionByDate(List<MetricsVolume> metricsVolumes) {
        Map<LocalDate, List<MetricsVolume>> distinctMetrics = new HashMap<>();
        metricsVolumes.forEach(metricsVolume -> {
            LocalDate metricsDate = LocalDateTime.ofInstant(metricsVolume.getTimestamp(), ZoneOffset.UTC).toLocalDate();
            if (distinctMetrics.containsKey(metricsDate)) {
                distinctMetrics.get(metricsDate).add(metricsVolume);
            } else {
                distinctMetrics.put(metricsDate, new ArrayList<>());
                distinctMetrics.get(metricsDate).add(metricsVolume);
            }
        });

        return distinctMetrics;
    }

    private UserInfo getAuthenticatedUser() {

        log.info("Method getAuthenticatedUser() called.");

        RrmToken authenticatedRrmToken = (RrmToken) SecurityContextHolder.getContext().getAuthentication();
        UserInfo authenticatedUserInfo = authenticatedRrmToken.getUserInfo();

        return authenticatedUserInfo;
    }

    private MetricsDataDto convertFromMetricsDataToMetricsDataDto(MetricsData metricsData) {

        MetricsDataDto metricsDataDto = new MetricsDataDto();
        metricsDataDto.setUptime(metricsData.getUptime());
        metricsDataDto.setHostname(metricsData.getHostname());
        metricsDataDto.setIp(metricsData.getIp());
        metricsDataDto.setImage(metricsData.getImage());
        metricsDataDto.setBseId(metricsData.getBseId());
        metricsDataDto.setRrmId(metricsData.getRrmId());
        metricsDataDto.setLastUpdated(metricsData.getLastUpdated());
        metricsDataDto.setContainerId(metricsData.getContainerId());
        metricsDataDto.setConsumerId(metricsData.getConsumerId());
        metricsDataDto.setConsumer(metricsData.getConsumer());
        metricsDataDto.setCpuConsumption(metricsData.getCpuConsumption().values().stream().flatMap(List::stream).collect(Collectors.toList()));
        metricsDataDto.setMemoryConsumption(metricsData.getMemoryConsumption().values().stream().flatMap(List::stream).collect(Collectors.toList()));

        return metricsDataDto;

    }

    private MetricsDataDto convertFromMetricsDataToMetricsDataDtoOnlyPeaks(MetricsData metricsData) {

        MetricsDataDto metricsDataDto = new MetricsDataDto();
        metricsDataDto.setUptime(metricsData.getUptime());
        metricsDataDto.setHostname(metricsData.getHostname());
        metricsDataDto.setIp(metricsData.getIp());
        metricsDataDto.setImage(metricsData.getImage());
        metricsDataDto.setBseId(metricsData.getBseId());
        metricsDataDto.setRrmId(metricsData.getRrmId());
        metricsDataDto.setLastUpdated(metricsData.getLastUpdated());
        metricsDataDto.setContainerId(metricsData.getContainerId());
        metricsDataDto.setConsumerId(metricsData.getConsumerId());
        metricsDataDto.setConsumer(metricsData.getConsumer());
        metricsData.getCpuConsumption().forEach((k, v) -> {
            metricsDataDto.getCpuConsumption().add(v.get(0));
        });


        metricsData.getMemoryConsumption().forEach((k, v) -> {
            metricsDataDto.getMemoryConsumption().add(v.get(0));
        });

        return metricsDataDto;
    }
}
