package eu.demeterh2020.resourceregistrymanagement.util.listener;

import eu.demeterh2020.resourceregistrymanagement.domain.Audit;
import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import eu.demeterh2020.resourceregistrymanagement.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DehResourceMongoEventListener extends AbstractMongoEventListener<DehResource> {

    @Autowired
    AuditService auditService;


    @Override
    public void onBeforeSave(BeforeSaveEvent<DehResource> event) {
        super.onBeforeSave(event);
        // TODO Extract owner from token
        // Set date of creation of a resource
        event.getSource().setCreateAt(LocalDateTime.now());
        event.getSource().setLastUpdate(LocalDateTime.now());
        event.getSource().setRating(0D);
    }

    /* Method for creating and storing Audit data for after DEHResource is stored in DB
     */
    @Override
    public void onAfterSave(AfterSaveEvent<DehResource> event) {
        super.onAfterSave(event);

        Map<String, LocalDateTime> versions = new HashMap<String, LocalDateTime>() {
            {
                put(event.getSource().getVersion(), event.getSource().getCreateAt());
            }
        };

        Audit newAuditResource = new Audit(event.getSource().getUid(), event.getSource().getCreateAt(),
                event.getSource().getCreateAt(), versions, new ArrayList<>(), 0D);

         auditService.save(newAuditResource);
    }

    /* Method for deleting Audit data after DEHResource is deleted from DB
     */
    @Override
    public void onAfterDelete(AfterDeleteEvent<DehResource> event) {
        super.onAfterDelete(event);

        auditService.deleteByResourceUid(event.getSource().get("_id").toString());
    }
}
