package eu.demeterh2020.resourceregistrymanagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Use this exception to send message to the world
 */
public class RRMException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private HttpStatus httpStatus;

    public RRMException() {
        super();
    }

    public RRMException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RRMException(String message, Throwable cause) {
        super(message, cause);
    }

    public RRMException(String message) {
        super(message);
    }

    public RRMException(Throwable cause) {
        super(cause);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}