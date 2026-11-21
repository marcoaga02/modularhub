package com.marcoaga02.modularhub.core.api.exception;

import com.marcoaga02.modularhub.core.api.dto.ErrorDTO;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public static final String FIELDS_PARAM = "fields";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        List<String> fields = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getField)
                .distinct()
                .toList();

        LOGGER.warn("Request validation failed for fields: {}", fields);
        return response(HttpStatus.BAD_REQUEST, ExceptionCodes.Validation.INVALID_FIELDS, Map.of(FIELDS_PARAM, fields));
    }

    @ExceptionHandler({
            HandlerMethodValidationException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorDTO> handleInvalidRequestException(Exception exception) {
        LOGGER.warn("Invalid request: {}", exception.getMessage());
        return response(HttpStatus.BAD_REQUEST, ExceptionCodes.Validation.INVALID_REQUEST, Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDTO> handleNoResourceFoundException(NoResourceFoundException exception) {
        LOGGER.warn("No resource found: {}", exception.getMessage());
        return response(HttpStatus.NOT_FOUND, ExceptionCodes.Generic.NOT_FOUND, Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDTO> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        LOGGER.warn("Method not supported: {}", exception.getMessage());

        HttpHeaders headers = new HttpHeaders();
        headers.setAllow(exception.getSupportedHttpMethods());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(headers)
                .body(errorDTO(ExceptionCodes.Generic.METHOD_NOT_ALLOWED, Map.of()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDTO> handleNotFoundException(NotFoundException exception) {
        LOGGER.warn("[{}] {}", exception.getErrorCode(), exception.getMessage());
        return response(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDTO> handleBadRequestException(BadRequestException exception) {
        LOGGER.warn("[{}] {}", exception.getErrorCode(), exception.getMessage());
        return response(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorDTO> handleConflictException(ConflictException exception) {
        LOGGER.warn("[{}] {}", exception.getErrorCode(), exception.getMessage());
        return response(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorDTO> handleForbiddenException(ForbiddenException exception) {
        LOGGER.warn("[{}] {}", exception.getErrorCode(), exception.getMessage());
        return response(HttpStatus.FORBIDDEN, exception);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorDTO> handleServiceUnavailableException(ServiceUnavailableException exception) {
        LOGGER.error("[{}] {}", exception.getErrorCode(), exception.getMessage(), exception);
        return response(HttpStatus.SERVICE_UNAVAILABLE, exception);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorDTO> handleApplicationException(ApplicationException exception) {
        LOGGER.error("[{}] {}", exception.getErrorCode(), exception.getMessage(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDTO> handleAccessDeniedException(AccessDeniedException exception) {
        LOGGER.warn("Access denied: {}", exception.getMessage());
        return response(HttpStatus.FORBIDDEN, ExceptionCodes.Access.DENIED, Map.of());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorDTO> handleOptimisticLockingFailureException(OptimisticLockingFailureException exception) {
        LOGGER.warn("Concurrent modification: {}", exception.getMessage());
        return response(HttpStatus.CONFLICT, ExceptionCodes.Generic.CONCURRENT_MODIFICATION, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleException(Exception exception) {
        LOGGER.error("Unhandled exception", exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionCodes.Internal.ERROR, Map.of());
    }

    public static ErrorDTO errorDTO(String errorCode, Map<String, Object> params) {
        return new ErrorDTO()
                .timestamp(OffsetDateTime.now())
                .errorCode(errorCode)
                .params(params);
    }

    private static ResponseEntity<ErrorDTO> response(HttpStatus status, ApplicationException exception) {
        return response(status, exception.getErrorCode(), exception.getParams());
    }

    private static ResponseEntity<ErrorDTO> response(HttpStatus status, String errorCode, Map<String, Object> params) {
        return ResponseEntity.status(status).body(errorDTO(errorCode, params));
    }
}
