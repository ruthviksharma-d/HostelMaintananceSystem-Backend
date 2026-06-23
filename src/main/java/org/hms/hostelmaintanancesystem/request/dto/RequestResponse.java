package org.hms.hostelmaintanancesystem.request.dto;

import lombok.Builder;
import lombok.Data;
import org.hms.hostelmaintanancesystem.request.RequestCategory;
import org.hms.hostelmaintanancesystem.request.RequestStatus;

import java.time.LocalDateTime;

/**
 * DTO for returning maintenance request data in API responses.
 *
 * Flattens the response so we don't return the full User object
 * (which might expose sensitive data or cause circular serialization issues).
 * We just return the creator's name and email.
 */
@Data
@Builder
public class RequestResponse {

    private Long id;
    private String title;
    private String description;
    private RequestCategory category;
    private RequestStatus status;

    // Flattened user data
    private Long createdById;
    private String createdByName;
    private String createdByEmail;

    // Resolution details
    private String resolutionNote;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;

    // Standard audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
