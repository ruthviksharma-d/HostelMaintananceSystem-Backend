package org.hms.hostelmaintanancesystem.common.exception;

import org.hms.hostelmaintanancesystem.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

/**
 * Global exception handler for the entire application.
 *
 * @ControllerAdvice -> Spring applies these handlers to ALL controllers.
 *                       Think of it as a catch block that wraps every endpoint.
 *
 * How it works:
 *   1. Controller or Service throws an exception.
 *   2. Spring scans @ControllerAdvice classes for a matching @ExceptionHandler.
 *   3. The handler converts the exception into a clean JSON response.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from @Valid annotations.
     *
     * Triggered when: @Valid RegisterRequest fails (e.g., blank email, short password).
     * Returns: 400 Bad Request with a comma-separated list of field errors.
     *
     * Example response:
     *   {
     *     "success": false,
     *     "message": "email: Please provide a valid email address, " +
     *                "password: Password must be between 6 and 100 characters"
     *   }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
    }

    /**
     * Handles duplicate email registration attempts.
     *
     * Triggered when: AuthService calls userRepository.existsByEmail() and finds a match.
     * Returns: 409 Conflict (standard HTTP code for resource conflicts).
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Catch-all for unexpected errors.
     *
     * Triggered when: Any exception not handled above.
     * Returns: 500 Internal Server Error.
     *
     * In production, you should log the stack trace here (don't expose it to clients).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        // TODO: Add logging (e.g., SLF4J logger.error("Unexpected error", ex))
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

}
