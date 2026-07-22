package com.taskbridge.projects.service;

import com.taskbridge.projects.domain.Project;
import com.taskbridge.projects.dto.*;
import com.taskbridge.projects.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // ---- Create ----------------------------------------------------------

    @Override
    public ProjectResponse create(UUID tenantId, String createdBy, CreateProjectRequest request) {
        requireTenantId(tenantId);
        requireUserId(createdBy);
        Objects.requireNonNull(request, "request must not be null");

        log.info("Creating project for tenant={} team={} by={}",
                 tenantId, request.teamId(), createdBy);

        Project project = Project.create(
            tenantId,
            request.teamId(),
            request.name(),
            request.description(),
            createdBy
        );

        Project saved = projectRepository.save(project);
        log.info("Project action=create outcome=success id={} tenant={} team={} by={}",
                 saved.getId(), tenantId, saved.getTeamId(), saved.getCreatedBy());
        return ProjectResponse.from(saved);
    }

    // ---- Update (fields) -------------------------------------------------

    @Override
    public ProjectResponse update(UUID tenantId, UUID projectId, UpdateProjectRequest request) {
        requireTenantId(tenantId);
        requireProjectId(projectId);
        Objects.requireNonNull(request, "request must not be null");

        log.info("Updating project id={} tenant={}", projectId, tenantId);

        Project project = findOwnedProject(tenantId, projectId);
        project.update(request.name(), request.description());

        Project saved = projectRepository.save(project);
        log.info("Project action=update outcome=success id={} tenant={} team={}",
                 saved.getId(), tenantId, saved.getTeamId());
        return ProjectResponse.from(saved);
    }

    // ---- Update status ---------------------------------------------------

    @Override
    public ProjectResponse updateStatus(UUID tenantId, UUID projectId,
                                        UpdateProjectStatusRequest request) {
        requireTenantId(tenantId);
        requireProjectId(projectId);
        Objects.requireNonNull(request, "request must not be null");

        log.info("Updating status of project id={} tenant={} to={}",
                 projectId, tenantId, request.status());

        Project project = findOwnedProject(tenantId, projectId);
        project.updateStatus(request.status());

        Project saved = projectRepository.save(project);
        log.info("Project action=update_status outcome=success id={} tenant={} status={}",
                 saved.getId(), tenantId, saved.getStatus());
        return ProjectResponse.from(saved);
    }

    // ---- Get by team -----------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getByTeam(UUID tenantId, UUID teamId) {
        requireTenantId(tenantId);
        requireTeamId(teamId);

        log.debug("Fetching projects for tenant={} team={}", tenantId, teamId);

        return projectRepository
            .findAllByTenantIdAndTeamIdOrderByCreatedAtDesc(tenantId, teamId)
            .stream()
            .map(ProjectResponse::from)
            .toList();
    }

    // ---- Delete ----------------------------------------------------------

    @Override
    public void delete(UUID tenantId, UUID projectId) {
        requireTenantId(tenantId);
        requireProjectId(projectId);

        log.info("Deleting project id={} tenant={}", projectId, tenantId);

        Project project = findOwnedProject(tenantId, projectId);
        projectRepository.delete(project);
        log.info("Project action=delete outcome=success id={} tenant={} team={}",
                 projectId, tenantId, project.getTeamId());
    }

    // ---- Internal helpers -----------------------------------------------

    /**
     * Loads the project and asserts it belongs to the current tenant.
     * Returns 404 (not 403) intentionally to avoid tenant enumeration.
     */
    private Project findOwnedProject(UUID tenantId, UUID projectId) {
        return projectRepository.findByIdAndTenantId(projectId, tenantId)
            .orElseThrow(() -> {
                log.warn("Project action=load outcome=not_found_or_tenant_mismatch id={} tenant={}",
                         projectId, tenantId);
                return new EntityNotFoundException(
                    "Project not found: " + projectId);
            });
    }

    private static void requireTenantId(UUID tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
    }

    private static void requireProjectId(UUID projectId) {
        Objects.requireNonNull(projectId, "projectId must not be null");
    }

    private static void requireTeamId(UUID teamId) {
        Objects.requireNonNull(teamId, "teamId must not be null");
    }

    private static void requireUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("createdBy must not be blank");
        }
    }
}

