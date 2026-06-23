package org.hms.hostelmaintanancesystem.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Status lifecycle of a maintenance request.
 *
 * Valid flow:
 *   OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED
 *
 * Rules (enforced in Service layer, Phase 7):
 *   - MAINTENANCE can move: OPEN -> IN_PROGRESS, IN_PROGRESS -> RESOLVED
 *   - TENANT can move:    RESOLVED -> CLOSED
 *   - CLOSED is terminal. No further changes.
 */
@Getter
@AllArgsConstructor
public enum RequestStatus {

    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    /**
     * Human-readable label for UI display.
     */
    private final String displayValue;

}
