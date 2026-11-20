package com.capgemini.assessment.petclinic.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Standardized immutable REST API response envelope providing consistent status, message, timestamp, and payload structure.
 *
 * @param <T> the type of the data payload returned by the endpoint
 */
@Schema(description = "Standardized API response envelope")
public record ApiResponseWrapper<T>(
        @Schema(description = "Indicates whether the operation succeeded", example = "true")
        boolean success,
        @Schema(description = "Human-readable summary message", example = "Operation completed successfully")
        String message,
        @Schema(description = "UTC timestamp of the response", example = "2026-11-15T15:00:00Z")
        Instant timestamp,
        @Schema(description = "Response data payload")
        T data
) {

    /**
     * Creates a successful API response containing a message and data payload.
     *
     * @param message descriptive success message
     * @param data response data object
     * @param <T> payload generic type
     * @return standardized ApiResponseWrapper instance
     */
    public static <T> ApiResponseWrapper<T> ok(String message, T data) {
        return new ApiResponseWrapper<>(true, message, Instant.now(), data);
    }

    /**
     * Creates an error API response with a message and null payload.
     *
     * @param message descriptive error message
     * @param <T> payload generic type
     * @return standardized ApiResponseWrapper representing an error state
     */
    public static <T> ApiResponseWrapper<T> error(String message) {
        return new ApiResponseWrapper<>(false, message, Instant.now(), null);
    }
}
