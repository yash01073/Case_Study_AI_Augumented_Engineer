package com.taskbridge.projects.repository;

import com.taskbridge.projects.domain.Project;
import com.taskbridge.projects.domain.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * All queries are tenant-scoped; no unbounded cross-tenant queries exist.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /** Find a project only if it belongs to the given tenant. */
    Optional<Project> findByIdAndTenantId(UUID id, UUID tenantId);

    /** All projects for a team, scoped to tenant. */
    List<Project> findAllByTenantIdAndTeamId(UUID tenantId, UUID teamId);

    /** All projects for a team filtered by status, scoped to tenant. */
    List<Project> findAllByTenantIdAndTeamIdAndStatus(UUID tenantId, UUID teamId, ProjectStatus status);

    /** Existence check scoped to tenant — used for soft-validation before write. */
    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}

