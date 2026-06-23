package org.hms.hostelmaintanancesystem.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Categories help the maintenance team prioritize and route requests.
 *
 * Example: ELECTRICAL failures are often high-priority (safety).
 */
@Getter
@AllArgsConstructor
public enum RequestCategory {

    ELECTRICAL("Electrical Issue"),
    PLUMBING("Plumbing Issue"),
    INTERNET("Internet / Wi-Fi Issue"),
    FURNITURE("Furniture Repair"),
    OTHER("Other");

    /**
     * Human-readable label for UI display.
     */
    private final String displayValue;

}
