package org.hms.hostelmaintanancesystem.user;

import jakarta.persistence.*;
import lombok.*;
import org.hms.hostelmaintanancesystem.common.BaseEntity;
import org.hms.hostelmaintanancesystem.common.Role;

/**
 * Represents a user (Tenant or Maintenance Staff) in the system.
 *
 * Inherits from BaseEntity:
 *   - id         (auto-generated BIGINT primary key)
 *   - createdAt  (auto-populated on insert)
 *   - updatedAt  (auto-populated on insert and update)
 *
 * Owns fields:
 *   - name       (required, max 100 chars)
 *   - email      (required, unique, used for login)
 *   - password   (required, stores BCrypt-encoded hash, NEVER plain text)
 *   - role       (required, TENANT or MAINTENANCE)
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    /**
     * Full name of the user.
     * @Column(nullable = false) -> DB rejects NULL.
     * length = 100             -> VARCHAR(100) instead of default 255.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Email address. Used as the login identifier.
     * unique = true            -> DB enforces no duplicates.
     * nullable = false         -> Cannot be NULL.
     * length = 255             -> Standard email max length.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * BCrypt-encoded password hash.
     * We NEVER store plain text passwords.
     * The encoder (in AuthService, Phase 4) produces ~60+ char strings.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Role determines what the user can do.
     *
     * STRING mapping is CRITICAL here. Integer mapping would be:
     *   DB stores 0, 1, 2...
     *   If enum order changes, the meaning in DB shifts silently.
     *   -> Disaster.
     *
     * STRING stores "TENANT", "MAINTENANCE" → readable, order-independent, safe.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

}
