package org.hms.hostelmaintanancesystem.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Abstract base class for all JPA entities.
 *
 * Provides common audit fields (id, createdAt, updatedAt) using
 * Spring Data JPA's auditing support.
 *
 * @MappedSuperclass          -> Fields are inherited by subclasses' tables.
 *                                No separate table is created for BaseEntity.
 * @EntityListeners(...)      -> Hooks into JPA lifecycle events (save, update).
 * @CreatedDate               -> Auto-populated on INSERT.
 * @LastModifiedDate          -> Auto-populated on INSERT and UPDATE.
 * @Column(updatable = false) -> createdAt can NEVER be changed after insert.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}
