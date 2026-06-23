package org.hms.hostelmaintanancesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * TEMPORARY Security Configuration for Phase 3.
 *
 * In Phase 5 (JWT Authentication), this will be replaced with:
 *   - JwtAuthenticationFilter added to the filter chain
 *   - .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
 *
 * Purpose NOW:
 *   1. Allow unauthenticated access to /api/auth/** (register, login)
 *   2. Keep all other endpoints protected (preparation for JWT)
 *   3. Disable formLogin and httpBasic (we're a REST API, not a web app)
 *   4. Stateless sessions (no server-side session storage)
 *
 * @Configuration        -> Marks this as a Spring config class.
 * @EnableWebSecurity    -> Enables Spring Security's web security support.
 *                           Without this, the SecurityFilterChain bean is ignored.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * PasswordEncoder bean using BCrypt.
     *
     * Why @Bean?
     *   Spring injects this wherever PasswordEncoder is needed (AuthService).
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
     * Defines the security filter chain.
     *
     * SecurityFilterChain is the modern Spring Security 6+ way.
     * The old WebSecurityConfigurerAdapter is deprecated.
     *
     * Request flow through this chain:
     *   Incoming Request
     *        ↓
     *   CSRF Filter (disabled for REST APIs)
     *        ↓
     *   Authentication checks
     *        ↓
     *   Authorization (permitAll vs authenticated)
     *        ↓
     *   Controller
     *
     * @param http  HttpSecurity DSL builder
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (Cross-Site Request Forgery)
                // REST APIs using tokens are not vulnerable to CSRF.
                // CSRF protection is for form submissions with session cookies.
                .csrf(AbstractHttpConfigurer::disable)

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Phase 3: Allow public access to auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Everything else requires authentication
                        // (Will be enforced by JWT filter in Phase 5-6)
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
                );

        return http.build();
    }

}
