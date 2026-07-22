package com.taskbridge.projects.api;

import com.taskbridge.projects.dto.CreateProjectRequest;
import com.taskbridge.projects.dto.ProjectResponse;
import com.taskbridge.projects.dto.UpdateProjectRequest;
import com.taskbridge.projects.dto.UpdateProjectStatusRequest;
import com.taskbridge.projects.service.command.CreateProjectCommand;
import com.taskbridge.projects.service.command.UpdateProjectCommand;
import com.taskbridge.projects.service.command.UpdateProjectStatusCommand;
import com.taskbridge.projects.service.result.ProjectView;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Maps HTTP-layer DTOs to application commands and application views to HTTP responses.
 */
@Component
public class ProjectApiMapper {

    /**
     * Creates a transport-neutral command for project creation.
     *
     * @param tenantId authenticated tenant identifier
     * @param createdBy authenticated actor identifier
     * @param request validated HTTP request payload
     * @return application command
     */
    public CreateProjectCommand toCreateCommand(UUID tenantId, String createdBy, CreateProjectRequest request) {
        return new CreateProjectCommand(
            tenantId,
            request.teamId(),
            request.name(),
            request.description(),
            createdBy
        );
    }

    /**
     * Creates a transport-neutral command for mutable project updates.
     *
     * @param tenantId authenticated tenant identifier
     * @param projectId target project identifier
     * @param request validated HTTP request payload
     * @return application command
     */
    public UpdateProjectCommand toUpdateCommand(UUID tenantId, UUID projectId, UpdateProjectRequest request) {
        return new UpdateProjectCommand(
            tenantId,
            projectId,
            request.name(),
            request.description()
        );
    }

    /**
     * Creates a transport-neutral command for project status updates.
     *
     * @param tenantId authenticated tenant identifier
     * @param projectId target project identifier
     * @param request validated HTTP request payload
     * @return application command
     */
    public UpdateProjectStatusCommand toUpdateStatusCommand(UUID tenantId, UUID projectId, UpdateProjectStatusRequest request) {
        return new UpdateProjectStatusCommand(tenantId, projectId, request.status());
    }

    /**
     * Converts an application view into the public REST response shape.
     *
     * @param view application view
     * @return response DTO
     */
    public ProjectResponse toResponse(ProjectView view) {
        return new ProjectResponse(
            view.id(),
            view.teamId(),
            view.name(),
            view.description(),
            view.status(),
            view.createdBy(),
            view.createdAt(),
            view.updatedAt()
        );
    }
}

