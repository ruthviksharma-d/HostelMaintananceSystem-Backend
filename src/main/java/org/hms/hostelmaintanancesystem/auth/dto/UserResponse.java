package org.hms.hostelmaintanancesystem.auth.dto;

import lombok.Builder;
import lombok.Data;
import org.hms.hostelmaintanancesystem.common.Role;

import java.time.LocalDateTime;

/**
 * DTO for returning user data in API responses.
 *
 * Why this exists (CRITICAL security concept):
 *   The User entity contains the PASSWORD field.
 *   If we return User directly from a controller, the password
 *   is serialized to JSON and sent to the client. → SECURITY BREACH.
 *
 *   UserResponse contains only SAFE fields: id, name, email, role, timestamps.
 *   Password is LIFTED OUT.
 *
 * Rule of thumb:
 *   Never return JPA entities directly from controllers.
 *   Always map to a DTO.
 */
@Data
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime createdAt;

}
