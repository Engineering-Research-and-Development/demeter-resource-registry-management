package eu.demeterh2020.resourceregistrymanagement.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsData {

    @JsonProperty("_id")
    private String containerId;
    @JsonProperty("consumerId")
    private String consumerId;
    @JsonProperty("consumer")
    private Author consumer;
    @JsonProperty("uptime")
    private Long uptime;
    @JsonProperty("hostname")
    private String hostname;
    @JsonProperty("ip")
    private String ip;
    @JsonProperty("image")
    private String image;
    @JsonProperty("BSE_ID")
    private String bseId;
    @JsonProperty("RRM_ID")
    private String rrmId;
    @JsonProperty("lastupdated")
    private Instant lastUpdated;
    @JsonProperty("cpu_percent")
    private Map<LocalDate, List<MetricsVolume>> cpuConsumption = new HashMap<>();
    @JsonProperty("mem_percent")
    private Map<LocalDate, List<MetricsVolume>> memoryConsumption = new HashMap<>();


}
