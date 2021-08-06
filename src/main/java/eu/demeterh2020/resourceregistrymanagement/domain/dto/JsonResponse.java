package eu.demeterh2020.resourceregistrymanagement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JsonResponse <T, S> {
    @Schema(example = "true")
    private boolean success;
    private String message;
    private T data;
    private S extraData;
}
