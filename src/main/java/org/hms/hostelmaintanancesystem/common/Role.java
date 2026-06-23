package org.hms.hostelmaintanancesystem.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the two roles in our system.
 *
 * TENANT      -> Can create/view/close their own requests.
 * MAINTENANCE -> Can view all requests and update status.
 */
@Getter
@AllArgsConstructor
public enum Role {

    TENANT("Tenant"),
    MAINTENANCE("Maintenance Staff");

    /**
     * Human-readable label for UI display or logging.
     */
    private final String displayValue;

}
