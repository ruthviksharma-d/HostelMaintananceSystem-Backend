package org.hms.hostelmaintanancesystem.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication responses (login and register).
 *
 * Wraps the JWT token along with user information so the client
 * gets everything it needs in a single response:
 *   - The token to include in subsequent requests
 *   - The token type (always "Bearer")
 *   - The user's safe profile data
 *
 * Example response:
 *   {
 *     "token": "eyJhbGciOiJIUzI1NiJ9...",
 *     "tokenType": "Bearer",
 *     "user": {
 *       "id": 1,
 *       "name": "John Doe",
 *       "email": "john@example.com",
 *       "role": "TENANT",
 *       "createdAt": "2024-01-15T10:30:00"
 *     }
 *   }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private UserResponse user;

}
