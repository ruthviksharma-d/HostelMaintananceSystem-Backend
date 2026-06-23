package org.hms.hostelmaintanancesystem.auth;

import lombok.RequiredArgsConstructor;
import org.hms.hostelmaintanancesystem.auth.dto.AuthResponse;
import org.hms.hostelmaintanancesystem.auth.dto.LoginRequest;
import org.hms.hostelmaintanancesystem.auth.dto.RegisterRequest;
import org.hms.hostelmaintanancesystem.auth.dto.UserResponse;
import org.hms.hostelmaintanancesystem.common.exception.DuplicateEmailException;
import org.hms.hostelmaintanancesystem.security.CustomUserDetails;
import org.hms.hostelmaintanancesystem.security.JwtService;
import org.hms.hostelmaintanancesystem.user.User;
import org.hms.hostelmaintanancesystem.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService handles authentication and registration business logic.
 *
 * @Service -> Tells Spring: "Manage the lifecycle of this class."
 *             Spring creates ONE instance (singleton) and injects it
 *             wherever @Autowired or constructor injection is used.
 *
 * @RequiredArgsConstructor -> Lombok generates a constructor with ALL final fields.
 *                              Spring uses this constructor for dependency injection.
 *                              Cleaner than @Autowired on fields.
 *
 * Why constructor injection over @Autowired fields?
 *   - Fields can be null if not injected (harder to spot bugs).
 *   - Constructor injection guarantees dependencies exist at object creation.
 *   - Easier to unit test (pass mocks to constructor, no reflection needed).
 *   - Final fields = immutable dependencies = thread-safe.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Authenticates a user with email and password, returns JWT.
     *
     * Flow:
     *   1. Create UsernamePasswordAuthenticationToken from request.
     *   2. Delegate to AuthenticationManager.
     *   3. AuthenticationManager -> DaoAuthenticationProvider
     *        -> CustomUserDetailsService.loadUserByUsername(email)
     *        -> PasswordEncoder.matches(rawPassword, storedHash)
     *   4. If credentials match, return Authentication object.
     *   5. Extract CustomUserDetails -> User -> generate JWT -> return AuthResponse.
     *
     * Throws:
     *   BadCredentialsException  -> if email not found OR password wrong
     *                               (Spring Security hides the difference to
     *                                prevent user enumeration attacks)
     *
     * @param request login credentials
     * @return AuthResponse with JWT token and user data
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .build();
    }

    /**
     * Registers a new user and returns JWT (so user is logged in immediately).
     *
     * Business Rules:
     *   1. Email must be unique (check DB first).
     *   2. Password must be hashed before storage (NEVER plain text).
     *   3. Role must be valid enum (enforced by RegisterRequest bean validation).
     *
     * @param request the registration input from client
     * @return AuthResponse with JWT token and user data
     * @throws DuplicateEmailException if email already exists
     */
    public AuthResponse register(RegisterRequest request) {
        // Business Rule #1: Unique email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(
                    "Email is already registered: " + request.getEmail()
            );
        }

        // Business Rule #2: Hash password
        // BCrypt automatically generates a salt and wraps it into the hash.
        // The hashed string looks like: $2a$10$...(60 characters total)
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Build the User entity
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(hashedPassword)  // HASHED, not plain
                .role(request.getRole())
                .build();

        // Save to database
        User savedUser = userRepository.save(user);

        // Generate JWT token for the new user
        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getRole().name()
        );

        // Return AuthResponse with token and user data
        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(savedUser))
                .build();
    }

    /**
     * Returns the currently authenticated user's profile.
     *
     * Extracts the user from Spring Security's SecurityContext,
     * which was set by JwtAuthenticationFilter during request processing.
     *
     * @return UserResponse with the current user's safe data
     * @throws RuntimeException if no user is authenticated (should never happen
     *                          because this endpoint requires authentication)
     */
    public UserResponse getCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return mapToUserResponse(userDetails.getUser());
    }

    /**
     * Maps User entity to UserResponse DTO.
     *
     * Why a private helper method?
     *   - Reusable: used by register, login, getMe, etc.
     *   - Single source of truth for how User -> UserResponse conversion works.
     *   - If we add a new field, we update ONE place, not every controller.
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
