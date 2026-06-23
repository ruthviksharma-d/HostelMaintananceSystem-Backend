package org.hms.hostelmaintanancesystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA Auditing across the entire application.
 *
 * Without this class, @CreatedDate and @LastModifiedDate in BaseEntity
 * would remain null because no one tells Spring to populate them.
 *
 * @EnableJpaAuditing -> Activates the AuditingEntityListener that
 *                       hooks into JPA's save() and update() operations.
 */
@Configuration
@EnableJpaAuditing
public class AuditingConfig {
    // No beans needed here. The annotation alone does the work.
}
