package eu.demeterh2020.resourceregistrymanagement.domain;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "resources_audit")
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
public class Audit {

    @Id
    private String uid;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdate;
    private Map<String, LocalDateTime> versions = new HashMap<>();
    private List<LocalDateTime> consumption = new ArrayList<>();
    private Double rating;

}
