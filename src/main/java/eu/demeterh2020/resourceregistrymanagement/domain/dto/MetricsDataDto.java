package eu.demeterh2020.resourceregistrymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.demeterh2020.resourceregistrymanagement.domain.MetricsVolume;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsDataDto {

    @JsonProperty("_id")
    private String containerId;
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
    private List<MetricsVolume> cpuConsumption = new ArrayList<>();
    @JsonProperty("mem_percent")
    private List<MetricsVolume> memoryConsumption = new ArrayList<>();

}
