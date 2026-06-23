package org.hms.hostelmaintanancesystem.request.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hms.hostelmaintanancesystem.request.RequestStatus;

/**
 * Data Transfer Object for updating a request's status (by Maintenance Staff).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequestStatusDTO {

    @NotNull(message = "Status is required")
    private RequestStatus status;

    // Optional: Only provided when resolving a request
    private String resolutionNote;

}
