package eu.demeterh2020.resourceregistrymanagement.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import eu.demeterh2020.resourceregistrymanagement.domain.Attachment;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service

public class AttachmentServiceImpl implements AttachmentService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Override
    public String saveAttachment(MultipartFile file) throws IOException {
        ObjectId id = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
        return id.toString();
    }

    @Override
    public Attachment getAttachment(String id) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        if (file != null) {
            Attachment attachment = new Attachment();
            attachment.setId(file.getObjectId().toString());
            attachment.setOriginalName(file.getFilename());
            attachment.setFileName(file.getFilename());
            attachment.setChunkSize(file.getChunkSize());
            attachment.setMetadata(file.getMetadata().toString());
            attachment.setLength(file.getLength());
            attachment.setUploadDate(file.getUploadDate());
            attachment.setContentType(file.getMetadata().get("_contentType").toString());
            return attachment;

        }
        return null;
    }

    @Override
    public void deleteAttachment(String id) {
        Query deleteAttachmentQuery = new Query().addCriteria(Criteria.where("_id").is(id));

        gridFsTemplate.delete(deleteAttachmentQuery);
    }

    @Override
    public GridFsResource streamAttachment(String id) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        if (file != null) {
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(file.getObjectId());
            GridFsResource gridFsResource = new GridFsResource(file, gridFSDownloadStream);
            return gridFsResource;
        }
        return null;
    }
}
