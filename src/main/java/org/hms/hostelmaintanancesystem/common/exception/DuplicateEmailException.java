package org.hms.hostelmaintanancesystem.common.exception;

/**
 * Custom exception thrown when a user tries to register with an email
 * that already exists in the database.
 *
 * Why custom exceptions instead of RuntimeException?
 *   - Self-documenting code: DuplicateEmailException tells you exactly what happened.
 *   - Fine-grained handling: @ControllerAdvice can map different exceptions
 *     to different HTTP status codes.
 *   - Business logic clarity: Service layer throws domain-specific errors.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

}
