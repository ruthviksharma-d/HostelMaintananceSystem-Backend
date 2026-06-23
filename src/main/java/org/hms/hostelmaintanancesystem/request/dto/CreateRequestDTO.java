package org.hms.hostelmaintanancesystem.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hms.hostelmaintanancesystem.request.RequestCategory;

/**
 * Data Transfer Object for creating a new maintenance request.
 *
 * We don't include status here because the service always defaults
 * it to OPEN when a tenant creates a request. We also don't include
 * createdBy, because we'll pull that from the authenticated token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private RequestCategory category;

}
