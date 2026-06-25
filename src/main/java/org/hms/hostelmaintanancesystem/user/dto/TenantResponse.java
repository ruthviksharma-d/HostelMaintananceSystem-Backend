package org.hms.hostelmaintanancesystem.user.dto;

import lombok.Builder;
import lombok.Data;
import org.hms.hostelmaintanancesystem.user.ApprovalStatus;

import java.time.LocalDateTime;

/**
 * DTO for returning tenant information in admin/management endpoints.
 */
@Data
@Builder
public class TenantResponse {

    private Long id;
    private String name;
    private String email;
    private ApprovalStatus approvalStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
