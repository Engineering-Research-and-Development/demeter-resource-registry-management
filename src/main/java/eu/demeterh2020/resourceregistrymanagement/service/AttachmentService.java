package eu.demeterh2020.resourceregistrymanagement.service;

import eu.demeterh2020.resourceregistrymanagement.domain.Attachment;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AttachmentService {

    /**
     * Method for storing Attachment in DB
     *
     * @param file - MultipartFile for storing
     * @return id of stored attachment
     */
    String saveAttachment(MultipartFile file) throws IOException;

    /**
     * Method for storing multiple attachments at once in DB
     *
     * @param attachments - list with MultipartFiles for storing
     * @return List of stored attachments
     */
    List<Attachment> saveMultipleAttachments(List<MultipartFile> attachments) throws IOException;

    /**
     * Method for getting Attachment by id from DB
     *
     * @param id - Attachment id
     * @return Optional Attachment
     */
    Attachment getAttachment(String id);

    /**
     * Method for deleting Attachment by id from DB
     *
     * @param id - DEH resource UID
     */
    void deleteAttachmentById(String id);

    /**
     * Method for deleting multiple Attachments at once from DB
     *
     * @param attachments - List with attachments for deleting
     */
    void deleteAttachments(List<Attachment> attachments);

    /**
     * Method streaming Attachment by id from DB
     *
     * @return gridFsResource for streaming
     */
    GridFsResource streamAttachment(String id);

}
