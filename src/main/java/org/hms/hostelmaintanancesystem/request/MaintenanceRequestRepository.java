package org.hms.hostelmaintanancesystem.request;

import org.hms.hostelmaintanancesystem.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for MaintenanceRequest entity.
 *
 * Extending JpaRepository<MaintenanceRequest, Long> provides standard CRUD
 * and pagination out of the box.
 *
 * Query Methods:
 *   Spring scans method names and generates JPQL/SQL automatically.
 *   Property names MUST match the Java entity field names.
 */
@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    /**
     * Finds all requests created by a specific user.
     *
     * Generated SQL:
     *   SELECT * FROM maintenance_requests WHERE created_by = ?
     *
     * Used for:
     *   - Tenant viewing their own requests (Phase 7)
     */
    List<MaintenanceRequest> findByCreatedBy(User user);

    /**
     * Finds all requests with a given status.
     *
     * Generated SQL:
     *   SELECT * FROM maintenance_requests WHERE status = ?
     *
     * Used for:
     *   - Maintenance dashboard filtering (Phase 8)
     */
    List<MaintenanceRequest> findByStatus(RequestStatus status);

    /**
     * Finds requests by a specific user AND status.
     *
     * Generated SQL:
     *   SELECT * FROM maintenance_requests WHERE created_by = ? AND status = ?
     *
     * Used for:
     *   - Tenant filtering their own requests by status
     */
    List<MaintenanceRequest> findByCreatedByAndStatus(User user, RequestStatus status);

    /**
     * Finds requests created by a specific user, ordered by creation time desc.
     *
     * Generated SQL:
     *   SELECT * FROM maintenance_requests WHERE created_by = ?
     *   ORDER BY created_at DESC
     *
     * Used for:
     *   - Tenant dashboard showing latest requests first
     */
    List<MaintenanceRequest> findByCreatedByOrderByCreatedAtDesc(User user);

    /**
     * Finds all requests ordered by creation time descending.
     *
     * Generated SQL:
     *   SELECT * FROM maintenance_requests ORDER BY created_at DESC
     *
     * Used for:
     *   - Maintenance staff viewing all requests (latest first)
     */
    List<MaintenanceRequest> findAllByOrderByCreatedAtDesc();

}
