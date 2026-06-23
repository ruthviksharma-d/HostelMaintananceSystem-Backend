package org.hms.hostelmaintanancesystem.request;

import jakarta.persistence.*;
import lombok.*;
import org.hms.hostelmaintanancesystem.common.BaseEntity;
import org.hms.hostelmaintanancesystem.user.User;

import java.time.LocalDateTime;

/**
 * Represents a maintenance request submitted by a tenant.
 *
 * Inherits from BaseEntity:
 *   - id         (auto-generated BIGINT primary key)
 *   - createdAt  (auto-populated timestamp on creation)
 *   - updatedAt  (auto-populated timestamp on creation and update)
 *
 * Owns fields:
 *   - title            (short summary of the issue)
 *   - description      (detailed explanation)
 *   - category         (ELECTRICAL, PLUMBING, etc.)
 *   - status           (OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED)
 *   - resolutionNote   (explanation from maintenance staff when resolving)
 *   - createdBy        (the tenant who created it -> FK users.id)
 *   - updatedBy        (the last person who modified it -> FK users.id)
 *   - resolvedAt       (when status became RESOLVED)
 *   - closedAt         (when status became CLOSED)
 */
@Entity
@Table(name = "maintenance_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRequest extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    /**
     * @Column without length -> defaults to VARCHAR(255).
     * For longer text, some databases map this to TEXT.
     * If you need explicit TEXT, use: @Column(nullable = false, columnDefinition = "TEXT")
     */
    @Column(nullable = false)
    private String description;

    /**
     * Category of the maintenance issue.
     * STRING enum mapping ensures DB stores "ELECTRICAL", not an integer.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestCategory category;

    /**
     * Current status in the lifecycle.
     * Default value: OPEN (set when a tenant creates a request).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.OPEN;

    /**
     * Resolution note added by maintenance staff.
     * Nullable because it's only populated when status = RESOLVED.
     */
    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    /**
     * The tenant who created this request.
     *
     * @ManyToOne         -> Many requests can belong to one user.
     * fetch = LAZY      -> Don't load the User row unless explicitly accessed.
     *                       Prevents unnecessary JOIN in SELECT queries.
     * @JoinColumn        -> Names the FK column "created_by" in the request table.
     * nullable = false  -> Every request MUST have a creator.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * The user (tenant or maintenance) who last modified this request.
     * Nullable initially since no one has updated it yet.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    /**
     * Timestamp when the request was resolved.
     * Populated by service layer when status transitions to RESOLVED.
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * Timestamp when the request was closed.
     * Populated by service layer when status transitions to CLOSED.
     */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

}
