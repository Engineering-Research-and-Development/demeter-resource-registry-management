package eu.demeterh2020.resourceregistrymanagement.exception;

import eu.demeterh2020.resourceregistrymanagement.domain.dto.JsonResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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


    HttpStatus successGlobal = HttpStatus.OK;

    /**
     * Handle global Exception. Triggered when an exception which is not covered in GlobalExceptionHandler occurs.
     *
     * @param ex      Exception
     * @param request HttpServletRequest
     * @return ApiError object
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<JsonResponse> handleException(Exception ex, HttpServletRequest request) {

        HttpStatus internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionData exceptionData = new ExceptionData(internalServerError, internalServerError.value(), ex.getMessage()
                , LocalDateTime.now(), request.getRequestURI());
        JsonResponse jsonResponse = new JsonResponse(false, "Internal Server error", null, exceptionData);

        return new ResponseEntity<>(jsonResponse, internalServerError);
    }

    /**
     * Handle BadRequest Exception. Triggered when an user sent a bad request.
     *
     * @param ex      BadRequestException
     * @param request HttpServletRequest
     * @return ApiError object
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<JsonResponse> badRequest(BadRequestException ex, HttpServletRequest request) {

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ExceptionData exceptionData = new ExceptionData(badRequest, badRequest.value(), ex.getMessage(), LocalDateTime.now()
                , request.getRequestURI());
        JsonResponse jsonResponse = new JsonResponse(false, "Bad request.", null, exceptionData);

        return new ResponseEntity<>(jsonResponse, successGlobal);
    }

    /**
     * Handle ResourceAlreadyExists Exception. Triggered when same DEH Resource exist in DB.
     *
     * @param ex      ResourceAlreadyExists
     * @param request HttpServletRequest
     * @return ApiError object
     */
    @ExceptionHandler(ResourceAlreadyExists.class)
    public ResponseEntity<JsonResponse> resourceAlreadyExists(ResourceAlreadyExists ex, HttpServletRequest request) {

        HttpStatus conflict = HttpStatus.CONFLICT;
        ExceptionData exceptionData = new ExceptionData(conflict, conflict.value(), ex.getMessage(), LocalDateTime.now()
                , request.getRequestURI());

        JsonResponse jsonResponse = new JsonResponse(false, "DEH Resource already exists.", null, exceptionData);
        return new ResponseEntity<>(jsonResponse, successGlobal);
    }

    /**
     * Handle ResourceNotFoundException Exception. Triggered when DEH Resource doesn't exists in DB.
     *
     * @param ex      ResourceNotFoundException
     * @param request HttpServletRequest
     * @return ApiError object
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<JsonResponse> resourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {

        HttpStatus notFound = HttpStatus.NOT_FOUND;
        ExceptionData exceptionData = new ExceptionData(notFound, notFound.value(), ex.getMessage(), LocalDateTime.now()
                , request.getRequestURI());
        JsonResponse jsonResponse = new JsonResponse(false, "DEH Resource not found.", null, exceptionData);

        return new ResponseEntity<>(jsonResponse, successGlobal);
    }

    /**
     * Handle UnauthorizedException Exception. Triggered when authenticated user is not allowed to perform an action.
     *
     * @param ex      UnauthorizedException
     * @param request HttpServletRequest
     * @return ApiError object
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<JsonResponse> unauthorized(UnauthorizedException ex, HttpServletRequest request) {

        HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
        ExceptionData exceptionData = new ExceptionData(unauthorized, unauthorized.value(), ex.getMessage(), LocalDateTime.now(), request.getRequestURI());

        JsonResponse jsonResponse = new JsonResponse(false, "Unauthorized! Not allowed to perform action.", null, exceptionData);

        return new ResponseEntity<>(jsonResponse, successGlobal);
    }

    /**
     * Handle ServletRequestBindingException Exception. Triggered when a parameter is missing.
     *
     * @param ex      ServletRequestBindingException
     * @param request HttpServletRequest
     * @return ApiError object
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<JsonResponse> handleMissingParams(ServletRequestBindingException ex, HttpServletRequest request) {

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ExceptionData exceptionData = new ExceptionData(badRequest, badRequest.value(), ex.getMessage(), LocalDateTime.now()
                , request.getRequestURI());
        JsonResponse jsonResponse = new JsonResponse(false, "Bad request.", null, exceptionData);

        return new ResponseEntity<>(jsonResponse, successGlobal);
    }

    /**
     * Handle MethodArgumentNotValidException Exception. Triggered when a validation of parameters occurs.
     *
     * @param ex      MethodArgumentNotValidException
     * @param request HttpServletRequest
     * @return ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ExceptionData exceptionData = new ExceptionData(badRequest, badRequest.value(), ex.getMessage(), LocalDateTime.now());

        JsonResponse jsonResponse = new JsonResponse(false, "Bad request.", null, exceptionData);

        return new ResponseEntity<>(jsonResponse, successGlobal);
    }

    /**
     * Handle HttpMessageNotReadableException Exception. Triggered when a request body is not readable.
     *
     * @param ex      HttpMessageNotReadableException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        String error = "Malformed JSON request";

        ExceptionData exceptionData = new ExceptionData(badRequest, badRequest.value(), error, LocalDateTime.now());

        JsonResponse jsonResponse = new JsonResponse(false, "Bad request.", null, exceptionData);

        return new ResponseEntity<>(jsonResponse, successGlobal);
    }

    /**
     * Handle MissingServletRequestParameterException. Triggered when a 'required' request parameter is missing.
     *
     * @param ex      MissingServletRequestParameterException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        String error = ex.getParameterName() + " parameter is missing";

        ExceptionData exceptionData = new ExceptionData(badRequest, badRequest.value(), error, LocalDateTime.now());

        JsonResponse jsonResponse = new JsonResponse(false, "Bad request.", null, exceptionData);

        return new ResponseEntity<>(jsonResponse, successGlobal);
    }

    /**
     * Handle HttpMediaTypeNotSupportedException. This one triggers when JSON is invalid as well.
     *
     * @param ex      HttpMediaTypeNotSupportedException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        HttpStatus unsupportedMediaType = HttpStatus.UNSUPPORTED_MEDIA_TYPE;

        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));

        ExceptionData exceptionData = new ExceptionData(unsupportedMediaType, unsupportedMediaType.value(), builder.substring(0, builder.length() - 2), LocalDateTime.now());

        JsonResponse jsonResponse = new JsonResponse(false, "Unsupported Media Type.", null, exceptionData);

        return new ResponseEntity<>(jsonResponse, successGlobal);
    }
}
