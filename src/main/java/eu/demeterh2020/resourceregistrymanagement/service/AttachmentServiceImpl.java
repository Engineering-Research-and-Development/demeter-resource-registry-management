package eu.demeterh2020.resourceregistrymanagement.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import eu.demeterh2020.resourceregistrymanagement.domain.Attachment;
import eu.demeterh2020.resourceregistrymanagement.logging.Loggable;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service

public class AttachmentServiceImpl implements AttachmentService {

    private final static Logger log = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public String saveAttachment(MultipartFile file) throws IOException {
        ObjectId id = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType());

        log.info("SAVED ATTACHMENT ID ", id);
        return id.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public Attachment getAttachment(String id) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));

        if (file != null) {
            log.info("ATTACHMENT WITH ID: " + id +  "FOUND!");
            Attachment attachment = new Attachment();
            attachment.setId(file.getObjectId().toString());
            attachment.setOriginalName(file.getFilename());
            attachment.setFileName(file.getFilename());
            attachment.setChunkSize(file.getChunkSize());
            attachment.setLength(file.getLength());
            attachment.setUploadDate(file.getUploadDate());
            attachment.setContentType(file.getMetadata().get("_contentType").toString());

            return attachment;
        }
        log.info("ATTACHMENT NOT FOUND!");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void deleteAttachmentById(String id) {
        Query deleteAttachmentQuery = new Query().addCriteria(Criteria.where("_id").is(id));

        gridFsTemplate.delete(deleteAttachmentQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public void deleteAttachments(List<Attachment> attachments) {
        List<Query> queries = new ArrayList<>();
        attachments.forEach(attachment -> queries.add(new Query().addCriteria(Criteria.where("_id").is(attachment.getId()))));
        queries.forEach(query -> gridFsTemplate.delete(query));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public GridFsResource streamAttachment(String id) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        if (file != null) {
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(file.getObjectId());
            GridFsResource gridFsResource = new GridFsResource(file, gridFSDownloadStream);

            return gridFsResource;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public List<Attachment> saveMultipleAttachments(List<MultipartFile> attachments) throws IOException {
        List<Attachment> savedAttachments = new ArrayList<>();

        for (MultipartFile uploadedFile : attachments) {
            String attachmentId = saveAttachment(uploadedFile);

            savedAttachments.add(getAttachment(attachmentId));
        }
        return savedAttachments;
    }
}
