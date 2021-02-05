package eu.demeterh2020.resourceregistrymanagement.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "deh_resource")
@Schema(name = "DehResource", description = "DEH Resource Object")
public class DehResource implements Serializable {

    @Id
    @Schema(example = "Resource 1", description = "Resource autogenerated uid")
    private String uid;
    @Schema(example = "Resource 1", description = "Resource name", required = true)
    @NotNull(message = "Resource name can't be null")
    private String name;
    @Schema(example = "Service", description = "Resource type", required = true)
    @NotNull(message = "Resource type can't be null")
    private String type;
    @Schema(example = "[\"usiness Applications\"]", description = "Resource categories", defaultValue = "null")
    private List<String> category = new ArrayList<>();
    @Schema(example = "Lorem ipsum dolor sit amet", description = "Description of a resource", defaultValue = "null")
    private String description;
    @Schema(example = "https://jsonplaceholder.typicode.com/comments", description = "Endpoint where Resource is exposed", defaultValue = "null")
    private String endpoint;
    @Schema(example = "1", description = "Status of a resource. Available values: 1 - Published, 2 - Not published, 3 - Draft ", required = true, defaultValue = "1")
    @NotNull(message = "Resource status can't be null")
    @Min(value = 1, message = "Wrong status. Status of a resource. Available values: 1 - Published, 2 - Not published, 3 - Draft")
    @Max(3)
    private int status;
    @Schema(example = "1.0", description = "Version of a resource", defaultValue = "null")
    private String version;
    @Schema(example = "1", description = "Maturity level of a resource", defaultValue = "null")
    private int maturityLevel;
    @Schema(example = "1234-uidas-123", description = "Resource owner id")
    @NotNull(message = "Resource owner can't be null")
    private String owner;
    @Schema(example = "[\"Applications\"]", description = "Resource tags", defaultValue = "null")
    private List<String> tags = new ArrayList<>();
    @Schema(description = "WILL BE AVAILABLE IN NEXT RELEASE")
    private Attachment attachment;
    @Schema(example = "3.2", description = "Resource rating", defaultValue = "0.0")
    private Double rating;
    @Schema(example = "[{ \"type\": \"Point\", \"coordinates\" : [ 0.0, 0.0 ]}]", description = "Resource location", defaultValue = "null")
    private List<GeoJsonPoint> localisation = new ArrayList<>();
    @Schema(example = "1", description = "Accessibility of a resource. Available values: 0 - Public, 1 - Private, 2 - Restricted ", defaultValue = "0")
    @Min(0)
    @Max(2)
    private int accessibility;
    @Schema(example = "2021-02-01T08:49:18.154Z", description = "Date and time of resource creation")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createAt;
    @Schema(example = "2021-02-01T08:49:18.154Z", description = "Date and time of resource last update")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lastUpdate;
    @Schema(example = "[\"Resource 2\"]", description = "Resource dependencies", defaultValue = "null")
    private List<String> dependencies = new ArrayList<>();
    @Schema(example = "[\"read\"]", description = "Resource Access Control Policies", defaultValue = "null")
    private List<String> accessControlPolicies = new ArrayList<>();
    @Schema(example = "www.google.com", description = "URL of a resource", defaultValue = "null")
    private String url;
    @Schema(description = "NOT AVAILABLE IN THIS VERSION")
    private List<String> billingInformation = new ArrayList<>();
    @Schema(example = "\"2021-02-01\": 1, \"2021-01-01\": 3", description = "Resource download history.", defaultValue = "null")
    private Map<LocalDate, Integer> downloadsHistory = new HashMap<>();
}
