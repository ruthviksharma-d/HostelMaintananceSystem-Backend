package org.hms.hostelmaintanancesystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — runs ONCE per request, before Spring Security's
 * default UsernamePasswordAuthenticationFilter.
 *
 * Purpose:
 *   Intercepts incoming HTTP requests, extracts the JWT token from the
 *   Authorization header, validates it, and sets the authenticated user
 *   in Spring Security's SecurityContext.
 *
 * Request flow:
 *   Client sends: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 *        ↓
 *   This filter extracts and validates the token
 *        ↓
 *   If valid: loads UserDetails and sets SecurityContext
 *   If invalid/missing: passes the request through (Spring Security
 *                        will reject it if the endpoint requires auth)
 *
 * Why OncePerRequestFilter?
 *   Guarantees this filter executes exactly once per request, even if
 *   the request is forwarded or dispatched internally.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Core filter logic — executed for every HTTP request.
     *
     * Steps:
     *   1. Extract "Authorization" header.
     *   2. Check if it starts with "Bearer ".
     *   3. Extract the token (everything after "Bearer ").
     *   4. Extract the email from the token.
     *   5. If no one is authenticated yet in the SecurityContext:
     *      a. Load UserDetails from the database.
     *      b. Validate the token against the loaded user.
     *      c. If valid, create an authentication token and set it in the context.
     *   6. Continue the filter chain.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Get the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Step 2: If no header or not Bearer token, skip this filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the token (remove "Bearer " prefix)
        final String jwt = authHeader.substring(7);

        // Step 4: Extract email from token
        final String email;
        try {
            email = jwtService.extractEmail(jwt);
        } catch (Exception e) {
            // Malformed token — let the request continue without authentication
            filterChain.doFilter(request, response);
            return;
        }

        // Step 5: If email is extracted and no authentication exists in context
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 5a: Load user from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 5b: Validate token against the loaded user
            if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {

                // 5c: Create authentication token
                // UsernamePasswordAuthenticationToken is Spring Security's standard
                // authentication holder. We pass:
                //   - principal:   the UserDetails object
                //   - credentials: null (we already validated via JWT, no password needed)
                //   - authorities: the user's granted authorities (roles)
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Attach request details (remote IP, session ID, etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set the authentication in SecurityContext
                // After this, SecurityContextHolder.getContext().getAuthentication()
                // returns our authenticated user for this request.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 6: Continue the filter chain
        filterChain.doFilter(request, response);
    }

}
