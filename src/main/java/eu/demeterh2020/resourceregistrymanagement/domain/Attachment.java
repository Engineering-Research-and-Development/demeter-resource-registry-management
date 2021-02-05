package eu.demeterh2020.resourceregistrymanagement.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "attachment")
public class Attachment {

    @Id
    private String id;
    private String originalName;
    private String fileName;
    private String metadata;
    private int chunkSize;
    private Long length;
    private String md5;
    private Date uploadDate;
    private String contentType;

}
