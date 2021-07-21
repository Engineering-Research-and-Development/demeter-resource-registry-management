package eu.demeterh2020.resourceregistrymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.demeterh2020.resourceregistrymanagement.domain.Author;
import eu.demeterh2020.resourceregistrymanagement.domain.MetricsVolume;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsDataDto {

    @NotNull
    @JsonProperty("_id")
    private String containerId;
    @JsonProperty("consumerId")
    private String consumerId;
    @JsonProperty("consumer")
    private Author consumer;
    @NotNull
    @JsonProperty("uptime")
    private Long uptime;
    @NotNull
    @JsonProperty("hostname")
    private String hostname;
    @NotNull
    @JsonProperty("ip")
    private String ip;
    @NotNull
    @JsonProperty("image")
    private String image;
    @JsonProperty("BSE_ID")
    private String bseId;
    @NotNull
    @JsonProperty("RRM_ID")
    private String rrmId;
    @NotNull
    @JsonProperty("lastupdated")
    private Instant lastUpdated;
    @NotNull
    @JsonProperty("cpu_percent")
    private List<MetricsVolume> cpuConsumption = new ArrayList<>();
    @NotNull
    @JsonProperty("mem_percent")
    private List<MetricsVolume> memoryConsumption = new ArrayList<>();

}
