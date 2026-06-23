package org.hms.hostelmaintanancesystem.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hms.hostelmaintanancesystem.common.Role;

/**
 * Data Transfer Object for user registration requests.
 *
 * Why DTOs matter:
 *   - Decouples the API contract from the database entity.
 *   - Prevents clients from sending fields like 'id' or 'createdAt'.
 *   - Bean Validation annotations enforce input rules before business logic runs.
 *
 * Validation Rules:
 *   - name:     required, 2-100 characters
 *   - email:    required, valid email format
 *   - password: required, 6-100 characters
 *   - role:     required, must be TENANT or MAINTENANCE
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * @NotBlank  -> Rejects null, empty string "", and whitespace-only.
     * @Size      -> Enforces length constraints before reaching the DB.
     */
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /**
     * @NotNull -> Rejects null, but allows any enum value.
     * The controller would return 400 if role is missing.
     */
    @NotNull(message = "Role is required")
    private Role role;

}
