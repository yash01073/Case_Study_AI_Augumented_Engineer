package com.taskbridge.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Immutable;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable audit entry that captures a tenant-scoped business event.
 * <p>
 * Audit entries are append-only and must never be updated or deleted through
 * the application. Previous and new state are stored as serialized JSON text.
 * </p>
 */
@Entity
@Immutable
@Table(
    name = "audit_entries",
    indexes = {
        @Index(name = "idx_audit_entries_org_time", columnList = "organization_id, occurred_at"),
        @Index(name = "idx_audit_entries_org_actor", columnList = "organization_id, actor"),
        @Index(name = "idx_audit_entries_org_event", columnList = "organization_id, event_type")
    }
)
public class AuditEntry {

    private static final int ACTOR_MAX_LENGTH = 255;
    private static final int STATE_MAX_LENGTH = 20000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @NotNull
    @Column(name = "project_id", nullable = false, updatable = false)
    private UUID projectId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false, length = 100)
    private AuditEventType eventType;

    @Size(max = STATE_MAX_LENGTH)
    @Column(name = "previous_state", columnDefinition = "TEXT", updatable = false)
    private String previousState;

    @Size(max = STATE_MAX_LENGTH)
    @Column(name = "new_state", columnDefinition = "TEXT", updatable = false)
    private String newState;

    @NotBlank
    @Size(max = ACTOR_MAX_LENGTH)
    @Column(name = "actor", nullable = false, updatable = false, length = 255)
    private String actor;

    @NotNull
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    protected AuditEntry() {
    }

    /**
     * Creates a new immutable audit entry.
     *
     * @param organizationId owning tenant/organization identifier
     * @param projectId      related project identifier
     * @param eventType      business event type
     * @param previousState  serialized previous state snapshot, may be {@code null}
     * @param newState       serialized new state snapshot, may be {@code null}
     * @param actor          authenticated actor identifier
     * @return new immutable audit entry
     */
    public static AuditEntry create(
        UUID organizationId,
        UUID projectId,
        AuditEventType eventType,
        String previousState,
        String newState,
        String actor
    ) {
        AuditEntry entry = new AuditEntry();
        entry.organizationId = Objects.requireNonNull(organizationId, "organizationId must not be null");
        entry.projectId = Objects.requireNonNull(projectId, "projectId must not be null");
        entry.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        entry.previousState = normalizeOptional(previousState, "previousState", STATE_MAX_LENGTH);
        entry.newState = normalizeOptional(newState, "newState", STATE_MAX_LENGTH);
        entry.actor = requireNonBlank(actor, "actor", ACTOR_MAX_LENGTH);
        return entry;
    }

    @PrePersist
    private void onCreate() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    private static String requireNonBlank(String value, String fieldName, int maxLength) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    private static String normalizeOptional(String value, String fieldName, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }

        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass()
            : getClass();
        Class<?> otherEffectiveClass = o instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();

        if (thisEffectiveClass != otherEffectiveClass) {
            return false;
        }

        AuditEntry other = (AuditEntry) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public String getPreviousState() {
        return previousState;
    }

    public String getNewState() {
        return newState;
    }

    public String getActor() {
        return actor;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}

