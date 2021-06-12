package eu.demeterh2020.resourceregistrymanagement.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsDto {


    private String rrmId;
    private String name;
    private int numberOfInstances;
    List<MetricsDataDto> containers = new ArrayList<>();

}
