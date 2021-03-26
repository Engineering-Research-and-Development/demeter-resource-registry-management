package eu.demeterh2020.resourceregistrymanagement.domain.dto;

import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JsonResponse {

    private boolean success;
    private String message;
    private List<DehResource> data;
    private Object extraData;


}
