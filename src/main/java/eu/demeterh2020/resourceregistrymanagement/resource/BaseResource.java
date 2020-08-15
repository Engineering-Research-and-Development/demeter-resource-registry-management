package eu.demeterh2020.resourceregistrymanagement.resource;

import eu.demeterh2020.resourceregistrymanagement.exception.RRMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseResource {

    private final static Logger log = LoggerFactory.getLogger(BaseResource.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody
    ResponseEntity<?> handleControllerException(HttpServletRequest request, Throwable ex) {

        HttpStatus status = getStatus(request, ex);

        Map<String, String> error = getResponse(request, ex, status);

        log.error("return {} for: {}", error, ex.getMessage());

        return new ResponseEntity<>(error, status);
    }

    private Map<String, String> getResponse(HttpServletRequest request, Throwable ex, HttpStatus status) {

        Map<String, String> error = new LinkedHashMap<>();

        error.put("timestamp", String.valueOf(System.currentTimeMillis()));
        error.put("status", String.valueOf(status));

        if (ex instanceof RRMException) {
            error.put("message", ex.getMessage());
        }

        error.put("path", request.getRequestURI());

        return error;
    }

    private HttpStatus getStatus(HttpServletRequest request, Throwable ex) {

        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

        if (ex instanceof RRMException) {

            if (((RRMException) ex).getHttpStatus() != null) {
                statusCode = ((RRMException) ex).getHttpStatus().value();
            }
        }

        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return HttpStatus.valueOf(statusCode);
        }
    }
}