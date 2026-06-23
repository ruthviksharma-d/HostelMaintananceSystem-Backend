package org.hms.hostelmaintanancesystem.common.exception;

/**
 * Custom exception thrown when a user tries to access a resource
 * they don't own.
 *
 * Examples:
 *   - A TENANT tries to view another tenant's maintenance request.
 *   - A TENANT tries to close a request that doesn't belong to them.
 *
 * Maps to HTTP 403 Forbidden in GlobalExceptionHandler.
 *
 * Why not just use AccessDeniedException?
 *   AccessDeniedException is for ROLE-based denial (e.g., tenant can't access admin endpoint).
 *   This exception is for OWNERSHIP-based denial (e.g., tenant can't view another tenant's request).
 *   Keeping them separate gives us clearer error messages.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

}
