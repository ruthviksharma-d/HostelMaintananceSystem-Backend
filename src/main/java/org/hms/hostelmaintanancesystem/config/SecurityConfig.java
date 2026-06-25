package org.hms.hostelmaintanancesystem.config;

import org.hms.hostelmaintanancesystem.security.CustomUserDetailsService;
import org.hms.hostelmaintanancesystem.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for JWT-based authentication with RBAC.
 *
 * Two layers of access control:
 *
 *   1. URL-level (this class):
 *      Coarse-grained rules based on roles.
 *      Example: Only MAINTENANCE can access GET /api/requests (all requests).
 *
 *   2. Method-level (@PreAuthorize in controllers/services):
 *      Fine-grained rules based on ownership.
 *      Example: TENANT can only view their OWN requests.
 *
 * @EnableMethodSecurity -> Activates @PreAuthorize, @PostAuthorize, @Secured.
 *   Without this, @PreAuthorize annotations are silently ignored!
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
     * Defines the security filter chain with JWT authentication and RBAC.
     *
     * Access control matrix:
     *
     *   PUBLIC (no JWT):
     *     POST /api/auth/register
     *     POST /api/auth/login
     *
     *   TENANT only:
     *     POST   /api/requests          (create a request)
     *     GET    /api/requests/my        (view own requests)
     *     PUT    /api/requests/{id}/close (close own resolved request)
     *
     *   MAINTENANCE only:
     *     GET    /api/requests           (view all requests)
     *     PUT    /api/requests/{id}/status (update request status)
     *
     *   ANY AUTHENTICATED:
     *     GET    /api/auth/me
     *     GET    /api/requests/{id}      (view single — ownership checked in service)
     *
     * Note: Fine-grained ownership checks (e.g., tenant can only see their own
     *       request details) are enforced at the SERVICE layer, not here.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — REST APIs using JWT tokens are not vulnerable
                .csrf(AbstractHttpConfigurer::disable)

                // Use CorsConfig so browser preflight requests from Vite are handled before auth.
                .cors(Customizer.withDefaults())

                // Authorization rules (evaluated top-to-bottom, first match wins)
                .authorizeHttpRequests(auth -> auth
                        // ── Public endpoints ──
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // ── TENANT-only endpoints ──
                        .requestMatchers(HttpMethod.POST, "/api/requests").hasRole("TENANT")
                        .requestMatchers(HttpMethod.GET, "/api/requests/my").hasRole("TENANT")
                        .requestMatchers(HttpMethod.PUT, "/api/requests/*/close").hasRole("TENANT")

                        // ── MAINTENANCE-only endpoints ──
                        .requestMatchers(HttpMethod.GET, "/api/requests").hasRole("MAINTENANCE")
                        .requestMatchers(HttpMethod.PUT, "/api/requests/*/status").hasRole("MAINTENANCE")
                        .requestMatchers("/api/admin/**").hasRole("MAINTENANCE")

                        // ── Everything else: just needs a valid JWT ──
                        .anyRequest().authenticated()
                )

                // Disable form login and HTTP Basic (we use JWT)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Stateless sessions — no server-side session storage
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // JWT filter runs before Spring's default auth filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

}
