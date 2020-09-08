package eu.demeterh2020.resourceregistrymanagement.util.listener;

import eu.demeterh2020.resourceregistrymanagement.domain.Audit;
import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;
import eu.demeterh2020.resourceregistrymanagement.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DEHResourceMongoEventListener extends AbstractMongoEventListener<DEHResource> {

    @Autowired
    AuditService auditService;

    @Override
    public void onBeforeSave(BeforeSaveEvent<DEHResource> event) {
        super.onBeforeSave(event);
        event.getSource().setCreateAt(LocalDateTime.now());
    }

    @Override
    public void onAfterSave(AfterSaveEvent<DEHResource> event) {
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

    @Override
    public void onAfterDelete(AfterDeleteEvent<DEHResource> event) {
        super.onAfterDelete(event);
        //TODO add business logic for deleting audit data after deleting DEHResource
    }


}
