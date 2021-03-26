package eu.demeterh2020.resourceregistrymanagement.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExceptionData {

    private HttpStatus httpStatus;
    private int code;
    private String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timestamp;
    private String path;

    public ExceptionData(HttpStatus httpStatus, String message, LocalDateTime timestamp) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.timestamp = timestamp;
    }

    public ExceptionData(HttpStatus httpStatus, int code, String message, LocalDateTime timestamp) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
    }

    public ExceptionData(HttpStatus httpStatus, String message, LocalDateTime timestamp, String path) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
    }
}