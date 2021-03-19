package eu.demeterh2020.resourceregistrymanagement.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdmToken implements Serializable {

    @JsonProperty("access_token")
    private String accessToken;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty("expires")
    private LocalDateTime expires;
    @JsonProperty("valid")
    private boolean valid;
    @JsonProperty("User")
    private UserInfo user;

}
