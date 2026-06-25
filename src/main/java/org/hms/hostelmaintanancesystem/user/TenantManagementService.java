package org.hms.hostelmaintanancesystem.user;

import lombok.RequiredArgsConstructor;
import org.hms.hostelmaintanancesystem.common.Role;
import org.hms.hostelmaintanancesystem.common.dto.PageResponse;
import org.hms.hostelmaintanancesystem.common.exception.ResourceNotFoundException;
import org.hms.hostelmaintanancesystem.common.exception.UnauthorizedAccessException;
import org.hms.hostelmaintanancesystem.user.dto.TenantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantManagementService {

    private final UserRepository userRepository;

    /** All tenants currently pending approval. */
    public PageResponse<TenantResponse> getPendingTenants(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> result = userRepository.findByRoleAndApprovalStatus(
                Role.TENANT, ApprovalStatus.PENDING, pageable);
        return PageResponse.of(result.map(this::mapToResponse));
    }

    /** All approved (active) tenants. */
    public PageResponse<TenantResponse> getApprovedTenants(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> result = userRepository.findByRoleAndApprovalStatus(
                Role.TENANT, ApprovalStatus.APPROVED, pageable);
        return PageResponse.of(result.map(this::mapToResponse));
    }

    /** Count of tenants awaiting approval — used for dashboard badge. */
    public long getPendingCount() {
        return userRepository.countByRoleAndApprovalStatus(Role.TENANT, ApprovalStatus.PENDING);
    }

    /** Approve a pending tenant. */
    public TenantResponse approveTenant(Long id) {
        User tenant = findTenantById(id);
        if (tenant.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new UnauthorizedAccessException("Tenant is already approved.");
        }
        tenant.setApprovalStatus(ApprovalStatus.APPROVED);
        return mapToResponse(userRepository.save(tenant));
    }

    /**
     * Reject a tenant (pending or active).
     * Sets status to REJECTED — soft deactivation; preserves request history.
     */
    public TenantResponse rejectTenant(Long id) {
        User tenant = findTenantById(id);
        tenant.setApprovalStatus(ApprovalStatus.REJECTED);
        return mapToResponse(userRepository.save(tenant));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User findTenantById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
        if (user.getRole() != Role.TENANT) {
            throw new UnauthorizedAccessException("This operation is only allowed for tenant accounts.");
        }
        return user;
    }

    private TenantResponse mapToResponse(User user) {
        return TenantResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .approvalStatus(user.getApprovalStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
