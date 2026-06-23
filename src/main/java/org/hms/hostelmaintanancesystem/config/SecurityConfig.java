package org.hms.hostelmaintanancesystem.config;

import org.hms.hostelmaintanancesystem.security.CustomUserDetailsService;
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

/**
 * TEMPORARY Security Configuration for Phase 3-4.
 *
 * In Phase 5 (JWT Authentication), this will be replaced with:
 *   - JwtAuthenticationFilter added to the filter chain
 *   - .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

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
     * AuthenticationProvider tells Spring Security HOW to authenticate.
     *
     * DaoAuthenticationProvider is the standard provider for username/password auth.
     * It needs two things:
     *   1. UserDetailsService -> loads user from DB by username/email
     *   2. PasswordEncoder    -> verifies raw password against stored hash
     *
     * Spring Security 7+ requires UserDetailsService in the constructor
     * (the no-arg constructor and setUserDetailsService() were removed).
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
     *
     * We use AuthenticationConfiguration which auto-discovers our provider bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
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
