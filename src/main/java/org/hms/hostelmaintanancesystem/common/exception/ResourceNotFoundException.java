package org.hms.hostelmaintanancesystem.common.exception;

/**
 * Custom exception thrown when a requested resource does not exist.
 *
 * Examples:
 *   - Maintenance request with given ID not found.
 *   - User with given ID not found.
 *
 * Maps to HTTP 404 Not Found in GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

}
