package com.taskbridge.projects.domain;

import jakarta.persistence.*;
import java.time.Instant;
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

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ProjectStatus status;

    @Column(name = "created_by", nullable = false, updatable = false, length = 255)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Project() {}

    /**
     * Factory method — enforces mandatory fields and sets defaults.
     */
    public static Project create(UUID tenantId, UUID teamId, String name,
                                 String description, String createdBy) {
        var project = new Project();
        project.tenantId    = tenantId;
        project.teamId      = teamId;
        project.name        = name;
        project.description = description;
        project.status      = ProjectStatus.DRAFT;
        project.createdBy   = createdBy;
        project.createdAt   = Instant.now();
        project.updatedAt   = Instant.now();
        return project;
    }

    // ---- Domain behaviour -----------------------------------------------

    /**
     * Transitions the project to the requested status.
     *
     * @throws IllegalStateException if the transition is not permitted.
     */
    public void updateStatus(ProjectStatus newStatus) {
        if (!isTransitionAllowed(this.status, newStatus)) {
            throw new IllegalStateException(
                "Transition from %s to %s is not allowed".formatted(this.status, newStatus));
        }
        this.status    = newStatus;
        this.updatedAt = Instant.now();
    }

    public void update(String name, String description) {
        this.name        = name;
        this.description = description;
        this.updatedAt   = Instant.now();
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

    public UUID getId()            { return id; }
    public UUID getTenantId()      { return tenantId; }
    public UUID getTeamId()        { return teamId; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public ProjectStatus getStatus() { return status; }
    public String getCreatedBy()   { return createdBy; }
    public Instant getCreatedAt()  { return createdAt; }
    public Instant getUpdatedAt()  { return updatedAt; }
}

