package eu.demeterh2020.resourceregistrymanagement.service;

import eu.demeterh2020.resourceregistrymanagement.domain.Attachment;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AttachmentService {

    String saveAttachment(MultipartFile file) throws IOException;

    Attachment getAttachment(String id);

    void deleteAttachmentById(String id);

    void deleteAttachments(List<Attachment> attachments);

    GridFsResource streamAttachment(String id);

}
