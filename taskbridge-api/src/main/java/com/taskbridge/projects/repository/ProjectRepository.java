package com.taskbridge.projects.repository;

import com.taskbridge.projects.domain.Project;
import com.taskbridge.projects.domain.ProjectStatus;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * All queries are tenant-scoped; no unbounded cross-tenant queries exist.
 */
@Repository
public interface ProjectRepository extends Repository<Project, UUID> {

    /** Persist a project aggregate. */
    <S extends Project> S save(S entity);

    /** Find a project only if it belongs to the given tenant. */
    Optional<Project> findByIdAndTenantId(UUID id, UUID tenantId);

    /** All projects for a team, scoped to tenant, newest first. */
    List<Project> findAllByTenantIdAndTeamIdOrderByCreatedAtDesc(UUID tenantId, UUID teamId);

    /** All projects for a team filtered by status, scoped to tenant. */
    List<Project> findAllByTenantIdAndTeamIdAndStatus(UUID tenantId, UUID teamId, ProjectStatus status);

    /** Existence check scoped to tenant — used for soft-validation before write. */
    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    /** Delete an owned project aggregate. */
    void delete(Project project);
}

