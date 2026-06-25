package org.hms.hostelmaintanancesystem.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Approval status for tenant accounts.
 *
 * Lifecycle:
 *   PENDING  -> Registered but awaiting maintenance staff approval.
 *   APPROVED -> Active account; full access granted.
 *   REJECTED -> Rejected or removed; access blocked.
 *
 * Maintenance staff accounts bypass this — they are always APPROVED.
 */
@Getter
@AllArgsConstructor
public enum ApprovalStatus {

    PENDING("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String displayValue;
}
