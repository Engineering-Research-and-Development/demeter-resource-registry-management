package eu.demeterh2020.resourceregistrymanagement.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Component
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ExceptionResponse> handleException(Exception ex, HttpServletRequest request) {

        HttpStatus badRequest = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponse exceptionResponse = new ExceptionResponse(badRequest, ex.getMessage(), LocalDateTime.now(), request.getRequestURI());

        return new ResponseEntity<>(exceptionResponse, badRequest);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> badRequest(BadRequestException ex, HttpServletRequest request) {

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ExceptionResponse exceptionResponse = new ExceptionResponse(badRequest, ex.getMessage(), LocalDateTime.now(), request.getRequestURI());

        return new ResponseEntity<>(exceptionResponse, badRequest);
    }

    @ExceptionHandler(ResourceAlreadyExists.class)
    public ResponseEntity<ExceptionResponse> resourceAlreadyExists(ResourceAlreadyExists ex, HttpServletRequest request) {

        HttpStatus conflict = HttpStatus.CONFLICT;
        ExceptionResponse exceptionResponse = new ExceptionResponse(conflict, ex.getMessage(), LocalDateTime.now(), request.getRequestURI());

        return new ResponseEntity<>(exceptionResponse, conflict);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> resourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {

        HttpStatus notFound = HttpStatus.NOT_FOUND;
        ExceptionResponse exceptionResponse = new ExceptionResponse(notFound, ex.getMessage(), LocalDateTime.now(), request.getRequestURI());

        return new ResponseEntity<>(exceptionResponse, notFound);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionResponse> unauthorized(UnauthorizedException ex, HttpServletRequest request) {

        HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
        ExceptionResponse exceptionResponse = new ExceptionResponse(unauthorized, ex.getMessage(), LocalDateTime.now(), request.getRequestURI());

        return new ResponseEntity<>(exceptionResponse, unauthorized);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ExceptionResponse> handleMissingParams(ServletRequestBindingException ex, HttpServletRequest request) {

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ExceptionResponse exceptionResponse = new ExceptionResponse(badRequest, ex.getMessage(), LocalDateTime.now(), request.getRequestURI());

        return new ResponseEntity<>(exceptionResponse, badRequest);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ExceptionResponse exceptionResponse = new ExceptionResponse(badRequest, ex.getMessage(), LocalDateTime.now());

        return new ResponseEntity<>(exceptionResponse, badRequest);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ExceptionResponse exceptionResponse = new ExceptionResponse(badRequest, ex.getMessage(), LocalDateTime.now());

        return new ResponseEntity<>(exceptionResponse, badRequest);
    }
}
