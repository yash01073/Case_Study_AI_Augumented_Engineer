package com.taskbridge.projects.service.command;

import com.taskbridge.projects.domain.ProjectStatus;

import java.util.UUID;

/**
 * Application command for updating project lifecycle status.
 *
 * @param tenantId  owning tenant identifier
 * @param projectId target project identifier
 * @param status    requested new project status
 */
public record UpdateProjectStatusCommand(
    UUID tenantId,
    UUID projectId,
    ProjectStatus status
) {
    /**
     * Validates required command state.
     */
    public UpdateProjectStatusCommand {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("projectId must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
    }
}

