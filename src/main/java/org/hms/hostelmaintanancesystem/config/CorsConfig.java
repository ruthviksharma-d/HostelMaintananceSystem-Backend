package org.hms.hostelmaintanancesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS (Cross-Origin Resource Sharing) configuration.
 *
 * Why do we need this?
 *   The frontend (React/Vite) will run on a different port (e.g., localhost:5173)
 *   or domain than the backend (e.g., localhost:8080 or api.hostel.com).
 *   Browsers block cross-origin requests for security reasons unless the backend
 *   explicitly allows them via CORS headers.
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Apply to all API endpoints
                        // Allow local dev (Vite default) and potential prod frontend URLs
                        .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
                        // Allow the standard HTTP methods we use
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // Allow standard headers plus Authorization (for JWT)
                        .allowedHeaders("Authorization", "Content-Type", "Accept")
                        // Required if the frontend needs to read headers from the response
                        // (e.g., if we were returning JWT in a header instead of body)
                        .exposedHeaders("Authorization")
                        // Allow credentials (cookies, authorization headers)
                        .allowCredentials(true)
                        // Cache the preflight OPTIONS request for 1 hour
                        .maxAge(3600);
            }
        };
    }
}
