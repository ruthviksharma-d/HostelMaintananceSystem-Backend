package org.hms.hostelmaintanancesystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hms.hostelmaintanancesystem.common.Role;
import org.hms.hostelmaintanancesystem.user.User;
import org.hms.hostelmaintanancesystem.user.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the default Maintenance Staff account on application startup.
 * Only creates the account if it does not already exist.
 *
 * Default credentials:
 *   Email:    admin@hostelfix.com
 *   Password: admin123
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String ADMIN_EMAIL    = "admin@hostelfix.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_NAME     = "Maintenance Admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            log.info("Default maintenance account already exists — skipping seed.");
            return;
        }

        User admin = User.builder()
                .name(ADMIN_NAME)
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(Role.MAINTENANCE)
                .phone("")
                .build();

        userRepository.save(admin);
        log.info("Default maintenance account created: {}", ADMIN_EMAIL);
    }
}
