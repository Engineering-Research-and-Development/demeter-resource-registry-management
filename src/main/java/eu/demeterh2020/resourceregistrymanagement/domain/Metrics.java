package eu.demeterh2020.resourceregistrymanagement.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "metrics")
public class Metrics {

    @Id
    private String id;
    private String rrmId;
    private String name;
    private String owner;
    private int numberOfInstances;
    List<MetricsData> containers = new ArrayList<>();

}
