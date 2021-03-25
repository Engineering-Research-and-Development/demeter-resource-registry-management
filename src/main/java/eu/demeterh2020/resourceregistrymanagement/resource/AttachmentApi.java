package eu.demeterh2020.resourceregistrymanagement.resource;

import eu.demeterh2020.resourceregistrymanagement.domain.Attachment;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceNotFoundException;
import eu.demeterh2020.resourceregistrymanagement.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping(value = "api/v1/attachment")
public class AttachmentApi {

    private final static Logger log = LoggerFactory.getLogger(DehResourceApi.class);

    @Autowired
    private AttachmentService attachmentService;

    @Operation(hidden = true)
    @GetMapping("/{id}")
    public Attachment getAttachment(@PathVariable String id) throws IllegalStateException {

        log.info("getAttachment() called.");

        Attachment attachment = attachmentService.getAttachment(id);

        return attachment;
    }

    @Operation(hidden = true)
    @GetMapping("/content/{id}")
    public void getAttachmentContent(@PathVariable String id, HttpServletResponse response) throws IllegalStateException, IOException {

        log.info("getAttachmentContent() called.");

        GridFsResource attachment = attachmentService.streamAttachment(id);

        if (attachment != null) {
            FileCopyUtils.copy(attachment.getInputStream(), response.getOutputStream());
        }
        throw new ResourceNotFoundException("Attachment with uid:" + id + " not found");
    }

    @Operation(hidden = true)
    @PostMapping(path = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addAttachment(@RequestParam("file") MultipartFile file) throws IOException {

        log.info("addAttachment() called.");

        String id = attachmentService.saveAttachment(file);

        return id;
    }

    @Operation(hidden = true)
    @DeleteMapping("/delete/{id}")
    public void deleteAttachment(@PathVariable String id) throws IOException {

        log.info("deleteAttachment() called.");

        attachmentService.deleteAttachmentById(id);
    }

    @Operation(hidden = true)
    @GetMapping(value = "/downloadFile/{id}")
    public void downloadFile(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws Exception {

        log.info("downloadFile() called.");

        GridFsResource attachment = attachmentService.streamAttachment(id);

        if (attachment != null) {
            String fileName = attachment.getFilename().replace(",", "");
            if (request.getHeader("User-Agent").toUpperCase().contains("MSIE") ||
                    request.getHeader("User-Agent").toUpperCase().contains("TRIDENT")
                    || request.getHeader("User-Agent").toUpperCase().contains("EDGE")) {
                fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
            } else {
                fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
            }
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
            response.addHeader("content-type", "application/octet-stream");
            IOUtils.copy(attachment.getInputStream(), response.getOutputStream());
        }

        throw new ResourceNotFoundException("Resource with uid:" + id + " not found");
    }
}
