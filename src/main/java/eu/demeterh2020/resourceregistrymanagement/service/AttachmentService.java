package eu.demeterh2020.resourceregistrymanagement.service;

import eu.demeterh2020.resourceregistrymanagement.domain.Attachment;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AttachmentService {

    String saveAttachment(MultipartFile file) throws IOException;

    Attachment getAttachment(String id);

    void deleteAttachment(String id);

    GridFsResource streamAttachment(String id);

}
