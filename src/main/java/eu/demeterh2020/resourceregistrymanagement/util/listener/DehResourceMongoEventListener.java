package eu.demeterh2020.resourceregistrymanagement.util.listener;

import eu.demeterh2020.resourceregistrymanagement.domain.Audit;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.service.AuditService;
import eu.demeterh2020.resourceregistrymanagement.service.DehResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DehResourceMongoEventListener extends AbstractMongoEventListener<DehResource> {

    @Autowired
    AuditService auditService;

    @Autowired
    DehResourceService dehResourceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<DehResource> event) {
        super.onBeforeConvert(event);

        //TODO extract token
        LocalDateTime now = LocalDateTime.now();
        // Set creation date and default rating of a resource if not exists in DB
        if (!dehResourceService.existByUid(event.getSource().getUid())) {
            event.getSource().setLastUpdate(now);
            event.getSource().setCreateAt(now);
            event.getSource().setRating(0D);
            event.getSource().setDownloadsHistory(new HashMap<>());
        }
    }


    /* Method for creating and storing Audit data for after DEHResource is stored in DB
     */
    @Override
    public void onAfterSave(AfterSaveEvent<DehResource> event) {
        super.onAfterSave(event);

        if (!auditService.existByResourceUid(event.getSource().getUid())) {
            Map<String, LocalDateTime> versions = new HashMap<String, LocalDateTime>() {
                {
                    put(event.getSource().getVersion(), event.getSource().getCreateAt());
                }
            };

            Audit newAuditResource = new Audit(event.getSource().getUid(), event.getSource().getCreateAt(),
                    event.getSource().getCreateAt(), versions, new ArrayList<>(), new ArrayList<>());

            auditService.save(newAuditResource);
        }
    }

    /* Method for deleting Audit data after DEHResource is deleted from DB
     */
    @Override
    public void onAfterDelete(AfterDeleteEvent<DehResource> event) {
        super.onAfterDelete(event);

        auditService.deleteByResourceUid(event.getSource().get("_id").toString());
    }
}
