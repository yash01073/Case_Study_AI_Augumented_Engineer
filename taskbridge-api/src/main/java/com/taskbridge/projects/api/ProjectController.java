package com.taskbridge.projects.api;

import com.taskbridge.projects.dto.*;
import com.taskbridge.projects.service.ProjectService;
import com.taskbridge.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API for Project management.
 * Tenant context is derived exclusively from the validated JWT via {@link TenantContext}.
 */
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * POST /api/v1/projects
     * Creates a new project for the authenticated tenant.
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> create(
        @Valid @RequestBody CreateProjectRequest request
    ) {
        UUID tenantId  = TenantContext.requireTenantId();
        String userId  = TenantContext.requireUserId();
        ProjectResponse response = projectService.create(tenantId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/projects/{id}
     * Updates name and description of a project.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateProjectRequest request
    ) {
        UUID tenantId = TenantContext.requireTenantId();
        return ResponseEntity.ok(projectService.update(tenantId, id, request));
    }

    /**
     * PATCH /api/v1/projects/{id}/status
     * Transitions project lifecycle status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjectResponse> updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateProjectStatusRequest request
    ) {
        UUID tenantId = TenantContext.requireTenantId();
        return ResponseEntity.ok(projectService.updateStatus(tenantId, id, request));
    }

    /**
     * GET /api/v1/projects?teamId={teamId}
     * Returns all projects for a team, scoped to the authenticated tenant.
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getByTeam(
        @RequestParam UUID teamId
    ) {
        UUID tenantId = TenantContext.requireTenantId();
        return ResponseEntity.ok(projectService.getByTeam(tenantId, teamId));
    }

    /**
     * DELETE /api/v1/projects/{id}
     * Permanently removes a project.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID tenantId = TenantContext.requireTenantId();
        projectService.delete(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}

