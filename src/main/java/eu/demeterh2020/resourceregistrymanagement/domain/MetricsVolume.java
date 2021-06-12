package eu.demeterh2020.resourceregistrymanagement.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsVolume {

    @JsonProperty("time_stamp")
    private Instant timestamp;
    @JsonAlias({"mem_percent", "cpu_percent"})
    private Double percent;
}
