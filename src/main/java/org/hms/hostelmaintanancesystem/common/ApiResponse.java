package org.hms.hostelmaintanancesystem.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Standardized wrapper for ALL API responses.
 *
 * Why this matters:
 *   - Clients get a predictable JSON structure on every endpoint.
 *   - No guessing whether success is a 200 + body or just 200.
 *   - Error responses follow the same shape as success responses.
 *
 * Response Format:
 *   Success:
 *     {
 *       "success": true,
 *       "message": "User registered successfully",
 *       "data": { "id": 1, "name": "John", ... },
 *       "timestamp": "2024-01-15T10:30:00"
 *     }
 *
 *   Error:
 *     {
 *       "success": false,
 *       "message": "Email already exists",
 *       "data": null,
 *       "timestamp": "2024-01-15T10:30:00"
 *     }
 *
 * @JsonInclude(JsonInclude.Include.NON_NULL)
 *   -> If data is null, the "data" field is OMITTED from JSON.
 *   Cleaner responses; no "data": null clutter.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Factory method for success responses.
     * Usage: return ApiResponse.success("User created", user);
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Factory method for error responses.
     * Usage: return ApiResponse.error("Email already exists");
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

}
