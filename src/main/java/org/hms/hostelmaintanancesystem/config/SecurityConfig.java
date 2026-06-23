package org.hms.hostelmaintanancesystem.config;

import org.hms.hostelmaintanancesystem.security.CustomUserDetailsService;
import org.hms.hostelmaintanancesystem.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for JWT-based authentication.
 *
 * Filter chain order:
 *   JwtAuthenticationFilter (our custom filter)
 *        ↓
 *   UsernamePasswordAuthenticationFilter (Spring's default, disabled for us)
 *        ↓
 *   Authorization filters
 *        ↓
 *   Controller
 *
 * Key decisions:
 *   - CSRF disabled: REST APIs using JWT tokens are not vulnerable to CSRF.
 *   - Stateless sessions: No server-side sessions; each request is independent.
 *   - Form login disabled: We use JWT, not HTML form login.
 *   - HTTP Basic disabled: We use JWT, not browser popups.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * PasswordEncoder bean using BCrypt.
     *
     * Why BCrypt?
     *   - Industry standard (OWASP recommends it)
     *   - Built-in salt (no need to manage salts manually)
     *   - Configurable strength (default is 10 rounds — about 100ms/hash)
     *   - Cannot be reversed to original password
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationProvider tells Spring Security HOW to authenticate.
     *
     * DaoAuthenticationProvider is the standard provider for username/password auth.
     * It needs two things:
     *   1. UserDetailsService -> loads user from DB by username/email
     *   2. PasswordEncoder    -> verifies raw password against stored hash
     *
     * Spring Security 7+ requires UserDetailsService in the constructor.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager is the GATEWAY to authentication.
     *
     * When AuthService calls authManager.authenticate(token),
     * the manager delegates to all registered AuthenticationProviders.
     * In our case: DaoAuthenticationProvider handles the token.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the security filter chain with JWT authentication.
     *
     * Request flow:
     *   Incoming Request
     *        ↓
     *   CSRF Filter (disabled for REST APIs)
     *        ↓
     *   JwtAuthenticationFilter (extracts + validates JWT)
     *        ↓
     *   Authorization checks (permitAll vs authenticated)
     *        ↓
     *   Controller
     *
     * Public endpoints (no JWT needed):
     *   - POST /api/auth/register
     *   - POST /api/auth/login
     *
     * Protected endpoints (valid JWT required):
     *   - GET /api/auth/me
     *   - Everything else
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (Cross-Site Request Forgery)
                // REST APIs using tokens are not vulnerable to CSRF.
                .csrf(AbstractHttpConfigurer::disable)

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — no authentication required
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Disable form-based login (the ugly HTML login page)
                .formLogin(AbstractHttpConfigurer::disable)

                // Disable HTTP Basic (browser popup for username/password)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Stateless session = no server-side session storage
                // Each request must carry its own authentication (JWT token)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Add our JWT filter BEFORE Spring's UsernamePasswordAuthenticationFilter
                // This ensures JWT is checked before any default auth mechanism
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

}
