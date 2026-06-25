package org.hms.hostelmaintanancesystem.user;

import lombok.RequiredArgsConstructor;
import org.hms.hostelmaintanancesystem.common.ApiResponse;
import org.hms.hostelmaintanancesystem.common.dto.PageResponse;
import org.hms.hostelmaintanancesystem.user.dto.TenantResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin endpoints for tenant account management.
 * All endpoints require MAINTENANCE role.
 */
@RestController
@RequestMapping("/api/admin/tenants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MAINTENANCE')")
public class TenantManagementController {

    private final TenantManagementService tenantManagementService;

    /** List tenants awaiting approval (paginated). */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PageResponse<TenantResponse>>> getPendingTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success("Pending tenants retrieved", tenantManagementService.getPendingTenants(page, size))
        );
    }

    /** Count of tenants awaiting approval — for dashboard badge. */
    @GetMapping("/pending/count")
    public ResponseEntity<ApiResponse<Long>> getPendingCount() {
        return ResponseEntity.ok(
                ApiResponse.success("Pending count retrieved", tenantManagementService.getPendingCount())
        );
    }

    /** List all approved (active) tenants (paginated). */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TenantResponse>>> getApprovedTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success("Tenants retrieved", tenantManagementService.getApprovedTenants(page, size))
        );
    }

    /** Approve a pending tenant — grants access to the system. */
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<TenantResponse>> approveTenant(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Tenant approved successfully", tenantManagementService.approveTenant(id))
        );
    }

    /**
     * Reject/remove a tenant — blocks access.
     * Uses soft deactivation (sets status to REJECTED) to preserve request history.
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<TenantResponse>> rejectTenant(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Tenant rejected successfully", tenantManagementService.rejectTenant(id))
        );
    }
}
