package eu.demeterh2020.resourceregistrymanagement.exception;

public class ResourceAlreadyExists extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public ResourceAlreadyExists(String message) {
        super(message);
    }

    public ResourceAlreadyExists(String message, Throwable cause) {
        super(message, cause);
    }
}
