package eu.demeterh2020.resourceregistrymanagement.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResourceMetricsDto {

    private String rrmId;
    private String name;
    private int numberOfInstances;

}

