package eu.demeterh2020.resourceregistrymanagement.domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "deh_resource")
@ApiModel
public class DEHResource {

    @Id
    private String uid;
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
    @NotNull(message = "Resource owner can't be null")
    private String owner;
    private List<String> tags = new ArrayList<>();
    private byte[] Attachment;
    private Double rating;
    private List<GeoJsonPoint> localisation = new ArrayList<>();
    private int accessibility;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lastUpdate;
    private List<String> dependencies = new ArrayList<>();
    private List<String> accessControlPolicies = new ArrayList<>();
    private String url;
    private List<String> billingInformation = new ArrayList<>();

}
