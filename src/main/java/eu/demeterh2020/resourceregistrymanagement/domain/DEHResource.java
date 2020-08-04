package eu.demeterh2020.resourceregistrymanagement.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "deh_resource")
public class DEHResource {

    @Id
    private String uid;
    private String name;
    private String type;
    private List<String> category;
    private String description;
    private String endpoint;
    private String status;
    private String version;
    private int maturityLevel;
    private String owner;
    private List<String> tags;
    private byte[] Attachment;
    private Double rating;
    private List<GeoJsonPoint> localisation;
    private int accessibility;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date lastUpdate = new Date();
    private List<String> dependencies;
    private List<String> accessControlPolicies;
    private String url;
    private List<String> billingInformation;

}
