package org.hms.hostelmaintanancesystem.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hms.hostelmaintanancesystem.auth.dto.RegisterRequest;
import org.hms.hostelmaintanancesystem.auth.dto.UserResponse;
import org.hms.hostelmaintanancesystem.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints.
 *
 * Base path: /api/auth
 *   POST /api/auth/register  -> Create a new user account
 *   (Phase 4 will add: POST /api/auth/login)
 *   (Phase 5 will add: GET /api/auth/me)
 *
 * @RestController       -> Convenience annotation for REST APIs.
 *                           Combines @Controller (Spring-managed bean)
 *                           and @ResponseBody (return values serialized to JSON).
 *
 * @RequestMapping(...)  -> Base URL path for ALL methods in this class.
 *
 * @RequiredArgsConstructor -> Constructor injection (same pattern as AuthService).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user (Tenant or Maintenance).
     *
     * Endpoint: POST /api/auth/register
     * Content-Type: application/json
     *
     * Request Body:
     *   {
     *     "name": "John Doe",
     *     "email": "john@example.com",
     *     "password": "password123",
     *     "role": "TENANT"
     *   }
     *
     * @Valid -> Triggers Bean Validation on RegisterRequest fields.
     *           If validation fails, MethodArgumentNotValidException is thrown,
     *           which GlobalExceptionHandler catches and returns 400 Bad Request.
     *
     * @RequestBody -> Tells Spring to deserialize the HTTP request body (JSON)
     *                 into the RegisterRequest Java object.
     *
     * Response: 201 Created
     *   {
     *     "success": true,
     *     "message": "User registered successfully",
     *     "data": {
     *       "id": 1,
     *       "name": "John Doe",
     *       "email": "john@example.com",
     *       "role": "TENANT",
     *       "createdAt": "2024-01-15T10:30:00"
     *     },
     *     "timestamp": "2024-01-15T10:30:05"
     *   }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @RequestBody @Valid RegisterRequest request) {

        UserResponse userResponse = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userResponse));
    }

}
