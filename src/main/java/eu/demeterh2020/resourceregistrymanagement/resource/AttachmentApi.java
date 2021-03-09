package eu.demeterh2020.resourceregistrymanagement.resource;

import eu.demeterh2020.resourceregistrymanagement.domain.Attachment;
import eu.demeterh2020.resourceregistrymanagement.exception.ResourceNotFoundException;
import eu.demeterh2020.resourceregistrymanagement.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.tomcat.util.http.fileupload.IOUtils;
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

    @Autowired
    private AttachmentService attachmentService;

    @Operation(hidden = true)
    @GetMapping("/{id}")
    public Attachment getAttachment(@PathVariable String id) throws IllegalStateException, IOException {
        Attachment video = attachmentService.getAttachment(id);

        return video;
    }

    @Operation(hidden = true)
    @GetMapping("/content/{id}")
    public void getAttachmentContent(@PathVariable String id, HttpServletResponse response) throws IllegalStateException, IOException {
        GridFsResource attachment = attachmentService.streamAttachment(id);

        if (attachment != null) {
            FileCopyUtils.copy(attachment.getInputStream(), response.getOutputStream());
        }
        throw new ResourceNotFoundException("Attachment with uid:" + id + " not found");
    }

    @Operation(hidden = true)
    @PostMapping(path = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addAttachment(@RequestParam("file") MultipartFile file) throws IOException {

        String id = attachmentService.saveAttachment(file);
        return id;
    }

    @Operation(hidden = true)
    @DeleteMapping("/delete/{id}")
    public void deleteAttachment(@PathVariable String id) throws IOException {

        attachmentService.deleteAttachmentById(id);
    }

    @Operation(hidden = true)
    @GetMapping(value = "/downloadFile/{id}")
    public void downloadFile(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
