package eu.demeterh2020.resourceregistrymanagement.domain.dto;

import eu.demeterh2020.resourceregistrymanagement.domain.Attachment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "DehResourceDTO", description = "Data object for creating DEH Resource")
public class DehResourceForCreationDTO {

    @Schema(example = "Resource 1", description = "Resource name", required = true)
    @NotNull(message = "Resource name can't be null")
    private String name;
    @Schema(example = "Service", description = "Resource type", required = true)
    @NotNull(message = "Resource type can't be null")
    private String type;
    @Schema(example = "[\"Business Applications\"]", description = "Resource categories", defaultValue = "null")
    private List<String> category = new ArrayList<>();
    @Schema(example = "Lorem ipsum dolor sit amet", description = "Description of a resource", defaultValue = "null")
    private String description;
    @Schema(example = "https://jsonplaceholder.typicode.com/comments", description = "Endpoint where Resource is exposed", defaultValue = "null")
    private String endpoint;
    @Schema(example = "1", description = "Status of a resource. Available values: 1 - Published, 2 - Not published, 3 - Draft ", required = true, defaultValue = "1")
    @NotNull(message = "Resource status can't be null")
    @Min(1)
    @Max(3)
    private int status;
    @Schema(example = "1.0", description = "Version of a resource", defaultValue = "null")
    private String version;
    @Schema(example = "1", description = "Maturity level of a resource", defaultValue = "null")
    private int maturityLevel;
    @Schema(example = "1234-uidas-123", description = "Resource owner id")
    private String owner;
    @Schema(example = "[\"Applications\"]", description = "Resource tags", defaultValue = "null")
    private List<String> tags = new ArrayList<>();
    @Schema(hidden = true)
    private Attachment attachment;
    @Schema(example = "[{ \"type\": \"Point\", \"coordinates\" : [ 0.0, 0.0 ]}]", description = "Resource location", defaultValue = "null")
    private List<GeoJsonPoint> localisation = new ArrayList<>();
    @Schema(example = "1", description = "Accessibility of a resource. Available values: 0 - Public, 1 - Private, 2 - Restricted ", defaultValue = "0")
    @Min(0)
    @Max(2)
    private int accessibility;
    @Schema(example = "[\"Resource 2\"]", description = "Resource dependencies", defaultValue = "null")
    private List<String> dependencies = new ArrayList<>();
    @Schema(example = "[\"read\"]", description = "Resource Access Control Policies (FIXED TO READ IN THIS VERSION)", defaultValue = "read")
    private List<String> accessControlPolicies = new ArrayList<>();
    @Schema(example = "www.google.com", description = "URL of a resource", defaultValue = "null")
    private String url;
}
