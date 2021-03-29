package eu.demeterh2020.resourceregistrymanagement.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JsonResponse {

    private boolean success;
    private String message;
    private Object data;
    private Object extraData;
}
