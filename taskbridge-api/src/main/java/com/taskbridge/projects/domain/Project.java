package com.taskbridge.projects.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Core Project aggregate root.
 * Every project is scoped to a tenant via {@code tenantId}.
 * All queries must include tenant_id to prevent cross-tenant data leakage.
 */
@Entity
@Table(
    name = "projects",
    indexes = {
        @Index(name = "idx_projects_tenant_id",     columnList = "tenant_id"),
        @Index(name = "idx_projects_tenant_team",   columnList = "tenant_id, team_id"),
        @Index(name = "idx_projects_tenant_status", columnList = "tenant_id, status")
    }
)
public class Project {

    private static final int NAME_MAX_LENGTH = 255;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;
    private static final int CREATED_BY_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @NotNull
    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @NotBlank
    @Size(max = NAME_MAX_LENGTH)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = DESCRIPTION_MAX_LENGTH)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ProjectStatus status;

    @NotBlank
    @Size(max = CREATED_BY_MAX_LENGTH)
    @Column(name = "created_by", nullable = false, updatable = false, length = 255)
    private String createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Project() {}

    /**
     * Factory method — enforces mandatory fields and sets defaults.
     */
    public static Project create(UUID tenantId, UUID teamId, String name,
                                 String description, String createdBy) {
        var project = new Project();
        project.tenantId    = Objects.requireNonNull(tenantId, "tenantId must not be null");
        project.teamId      = Objects.requireNonNull(teamId, "teamId must not be null");
        project.name        = requireNonBlank(name, "name", NAME_MAX_LENGTH);
        project.description = normalizeOptional(description, "description", DESCRIPTION_MAX_LENGTH);
        project.status      = ProjectStatus.DRAFT;
        project.createdBy   = requireNonBlank(createdBy, "createdBy", CREATED_BY_MAX_LENGTH);
        return project;
    }

    // ---- Domain behaviour -----------------------------------------------

    /**
     * Transitions the project to the requested status.
     *
     * @throws IllegalStateException if the transition is not permitted.
     */
    public void updateStatus(ProjectStatus newStatus) {
        Objects.requireNonNull(newStatus, "newStatus must not be null");
        if (!isTransitionAllowed(this.status, newStatus)) {
            throw new IllegalStateException(
                "Transition from %s to %s is not allowed".formatted(this.status, newStatus));
        }
        this.status = newStatus;
    }

    public void update(String name, String description) {
        this.name = requireNonBlank(name, "name", NAME_MAX_LENGTH);
        this.description = normalizeOptional(description, "description", DESCRIPTION_MAX_LENGTH);
    }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
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

    private static boolean isTransitionAllowed(ProjectStatus from, ProjectStatus to) {
        return switch (from) {
            case DRAFT     -> to == ProjectStatus.ACTIVE;
            case ACTIVE    -> to == ProjectStatus.ON_HOLD
                           || to == ProjectStatus.COMPLETED
                           || to == ProjectStatus.CANCELLED;
            case ON_HOLD   -> to == ProjectStatus.ACTIVE
                           || to == ProjectStatus.CANCELLED;
            case COMPLETED -> to == ProjectStatus.ARCHIVED;
            case ARCHIVED, CANCELLED -> false;
        };
    }

    // ---- Getters (no setters — mutations go through domain methods) ------

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

        Project other = (Project) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }

    public UUID getId()            { return id; }
    public UUID getTenantId()      { return tenantId; }
    public UUID getTeamId()        { return teamId; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public ProjectStatus getStatus() { return status; }
    public String getCreatedBy()   { return createdBy; }
    public Instant getCreatedAt()  { return createdAt; }
    public Instant getUpdatedAt()  { return updatedAt; }
    public long getVersion()       { return version; }
}

