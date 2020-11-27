package eu.demeterh2020.resourceregistrymanagement.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DehResourceForCreationDTO {

    @NotNull(message = "Resource name can't be null")
    private String name;
    @NotNull(message = "Resource type can't be null")
    private String type;
    private List<String> category = new ArrayList<>();
    private String description;
    private String endpoint;
    @NotNull(message = "Resource status can't be null")
    private String status;
    private String version;
    private int maturityLevel;
    private String owner;
    private List<String> tags = new ArrayList<>();
    private byte[] attachment;
    private List<GeoJsonPoint> localisation = new ArrayList<>();
    private int accessibility;
    private List<String> dependencies = new ArrayList<>();
    private List<String> accessControlPolicies = new ArrayList<>();
    private String url;

}
