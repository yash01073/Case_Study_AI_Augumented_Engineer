package com.taskbridge.projects.service;

import com.taskbridge.projects.domain.Project;
import com.taskbridge.projects.repository.ProjectRepository;
import com.taskbridge.projects.service.command.CreateProjectCommand;
import com.taskbridge.projects.service.command.DeleteProjectCommand;
import com.taskbridge.projects.service.command.UpdateProjectCommand;
import com.taskbridge.projects.service.command.UpdateProjectStatusCommand;
import com.taskbridge.projects.service.query.GetProjectsByTeamQuery;
import com.taskbridge.projects.service.result.ProjectView;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Default application service for tenant-scoped project operations.
 * <p>
 * This service owns transaction boundaries, structured business logging, and
 * tenant-aware repository access for the Project aggregate.
 * </p>
 */
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    public ProjectView create(CreateProjectCommand command) {
        validate(command);

        log.info("Creating project for tenant={} team={} by={}",
                 command.tenantId(), command.teamId(), command.createdBy());

        Project project = Project.create(
            command.tenantId(),
            command.teamId(),
            command.name(),
            command.description(),
            command.createdBy()
        );

        Project saved = projectRepository.save(project);
        log.info("Project action=create outcome=success id={} tenant={} team={} by={}",
                 saved.getId(), command.tenantId(), saved.getTeamId(), saved.getCreatedBy());
        return projectMapper.toView(saved);
    }

    @Override
    public ProjectView update(UpdateProjectCommand command) {
        validate(command);

        log.info("Updating project id={} tenant={}", command.projectId(), command.tenantId());

        Project project = findOwnedProject(command.tenantId(), command.projectId());
        project.update(command.name(), command.description());

        Project saved = projectRepository.save(project);
        log.info("Project action=update outcome=success id={} tenant={} team={}",
                 saved.getId(), command.tenantId(), saved.getTeamId());
        return projectMapper.toView(saved);
    }

    @Override
    public ProjectView updateStatus(UpdateProjectStatusCommand command) {
        validate(command);

        log.info("Updating status of project id={} tenant={} to={}",
                 command.projectId(), command.tenantId(), command.status());

        Project project = findOwnedProject(command.tenantId(), command.projectId());
        project.updateStatus(command.status());

        Project saved = projectRepository.save(project);
        log.info("Project action=update_status outcome=success id={} tenant={} status={}",
                 saved.getId(), command.tenantId(), saved.getStatus());
        return projectMapper.toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectView> getByTeam(GetProjectsByTeamQuery query) {
        validate(query);

        log.debug("Fetching projects for tenant={} team={}", query.tenantId(), query.teamId());

        return projectRepository
            .findAllByTenantIdAndTeamIdOrderByCreatedAtDesc(query.tenantId(), query.teamId())
            .stream()
            .map(projectMapper::toView)
            .toList();
    }

    @Override
    public void delete(DeleteProjectCommand command) {
        validate(command);

        log.info("Deleting project id={} tenant={}", command.projectId(), command.tenantId());

        Project project = findOwnedProject(command.tenantId(), command.projectId());
        projectRepository.delete(project);
        log.info("Project action=delete outcome=success id={} tenant={} team={}",
                 command.projectId(), command.tenantId(), project.getTeamId());
    }

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

    private static void validate(CreateProjectCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
    }

    private static void validate(UpdateProjectCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
    }

    private static void validate(UpdateProjectStatusCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
    }

    private static void validate(GetProjectsByTeamQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query must not be null");
        }
    }

    private static void validate(DeleteProjectCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
    }
}

