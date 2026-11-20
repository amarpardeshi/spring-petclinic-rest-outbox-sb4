package com.capgemini.assessment.petclinic.common.exception;

import com.capgemini.assessment.petclinic.common.dto.ApiResponseWrapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized REST exception translator ensuring consistent error payloads across all controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Default constructor for GlobalExceptionHandler.
     */
    public GlobalExceptionHandler() {
        // Default constructor
    }

    /**
     * Handles ResourceNotFoundException and returns HTTP 404 Not Found.
     *
     * @param exception the missing resource exception
     * @return HTTP 404 response wrapped in ApiResponseWrapper
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleResourceNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseWrapper.error(exception.getMessage()));
    }

    /**
     * Handles Jakarta Bean Validation failures and returns HTTP 400 Bad Request with field-level details.
     *
     * @param exception validation failure exception
     * @return HTTP 400 response containing field validation messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseWrapper<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        ApiResponseWrapper<Map<String, String>> body = new ApiResponseWrapper<>(
                false,
                "Request validation failed",
                java.time.Instant.now(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles IntegrationDeliveryException and returns HTTP 502 Bad Gateway.
     *
     * @param exception integration failure exception
     * @return HTTP 502 response wrapped in ApiResponseWrapper
     */
    @ExceptionHandler(IntegrationDeliveryException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleIntegrationDelivery(IntegrationDeliveryException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponseWrapper.error(exception.getMessage()));
    }

    /**
     * Handles unexpected runtime exceptions and returns HTTP 500 Internal Server Error.
     *
     * @param exception uncaught exception
     * @return HTTP 500 response wrapped in ApiResponseWrapper
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleGenericException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseWrapper.error("Unexpected server error: " + exception.getMessage()));
    }
}
