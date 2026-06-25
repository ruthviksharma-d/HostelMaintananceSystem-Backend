package org.hms.hostelmaintanancesystem.auth;

import lombok.RequiredArgsConstructor;
import org.hms.hostelmaintanancesystem.auth.dto.AuthResponse;
import org.hms.hostelmaintanancesystem.auth.dto.LoginRequest;
import org.hms.hostelmaintanancesystem.auth.dto.RegisterRequest;
import org.hms.hostelmaintanancesystem.auth.dto.UserResponse;
import org.hms.hostelmaintanancesystem.common.Role;
import org.hms.hostelmaintanancesystem.common.exception.AccountNotApprovedException;
import org.hms.hostelmaintanancesystem.common.exception.DuplicateEmailException;
import org.hms.hostelmaintanancesystem.common.exception.UnauthorizedAccessException;
import org.hms.hostelmaintanancesystem.security.CustomUserDetails;
import org.hms.hostelmaintanancesystem.security.JwtService;
import org.hms.hostelmaintanancesystem.user.ApprovalStatus;
import org.hms.hostelmaintanancesystem.user.User;
import org.hms.hostelmaintanancesystem.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
     * After credential verification, tenants are additionally checked for
     * approval status — PENDING and REJECTED tenants are blocked with a
     * descriptive 403 message.
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

        // Tenants must be APPROVED before they can access the system
        if (user.getRole() == Role.TENANT) {
            if (user.getApprovalStatus() == ApprovalStatus.PENDING) {
                throw new AccountNotApprovedException(
                        "Your hostel membership is awaiting approval by the maintenance team. " +
                        "You will be notified once your account is approved."
                );
            }
            if (user.getApprovalStatus() == ApprovalStatus.REJECTED) {
                throw new AccountNotApprovedException(
                        "Your account registration has been rejected. " +
                        "Please contact the hostel administration for more information."
                );
            }
        }

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
     * Registers a new tenant account in PENDING state.
     *
     * New tenants cannot log in until a maintenance staff member approves them.
     * Maintenance accounts cannot be self-registered.
     */
    public AuthResponse register(RegisterRequest request) {
        // Only tenants can self-register
        if (request.getRole() == Role.MAINTENANCE) {
            throw new UnauthorizedAccessException(
                    "Maintenance accounts cannot be self-registered. Please contact an administrator."
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(
                    "Email is already registered: " + request.getEmail()
            );
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // New tenants start as PENDING — @Builder.Default sets approvalStatus = PENDING
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(hashedPassword)
                .role(request.getRole())
                .phone("")
                .build();

        userRepository.save(user);

        // Do NOT issue a JWT — the user must wait for approval before logging in.
        // Return a response indicating that approval is required.
        return AuthResponse.builder()
                .token(null)
                .user(mapToUserResponse(user))
                .build();
    }

    /**
     * Returns the currently authenticated user's profile.
     */
    public UserResponse getCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return mapToUserResponse(userDetails.getUser());
    }

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
