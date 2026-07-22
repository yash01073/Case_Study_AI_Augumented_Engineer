package com.taskbridge.projects.service;

import com.taskbridge.projects.dto.*;

import java.util.List;
import java.util.UUID;

/**
 * Application-level contract for project operations.
 * All methods are tenant-scoped; callers must supply tenantId derived from JWT.
 */
public interface ProjectService {

    /**
     * Creates a new project for the given tenant.
     *
     * @param tenantId  tenant context from JWT
     * @param createdBy subject claim from JWT
     * @param request   validated creation payload
     * @return the persisted project as a response DTO
     */
    ProjectResponse create(UUID tenantId, String createdBy, CreateProjectRequest request);

    /**
     * Updates mutable fields (name, description) of an existing project.
     *
     * @param tenantId  tenant context from JWT
     * @param projectId target project ID
     * @param request   validated update payload
     * @return updated project as a response DTO
     */
    ProjectResponse update(UUID tenantId, UUID projectId, UpdateProjectRequest request);

    /**
     * Transitions a project to a new lifecycle status.
     *
     * @param tenantId  tenant context from JWT
     * @param projectId target project ID
     * @param request   desired status
     * @return updated project as a response DTO
     */
    ProjectResponse updateStatus(UUID tenantId, UUID projectId, UpdateProjectStatusRequest request);

    /**
     * Returns all projects belonging to the given team, scoped to tenant.
     *
     * @param tenantId tenant context from JWT
     * @param teamId   target team
     * @return list of projects (may be empty)
     */
    List<ProjectResponse> getByTeam(UUID tenantId, UUID teamId);

    /**
     * Permanently deletes a project. Validates tenant ownership before deletion.
     *
     * @param tenantId  tenant context from JWT
     * @param projectId target project ID
     */
    void delete(UUID tenantId, UUID projectId);
}

