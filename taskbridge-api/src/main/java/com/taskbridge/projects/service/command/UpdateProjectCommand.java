package com.taskbridge.projects.service.command;

import java.util.UUID;

/**
 * Application command for updating mutable project fields.
 *
 * @param tenantId    owning tenant identifier
 * @param projectId   target project identifier
 * @param name        new project name
 * @param description optional project description
 */
public record UpdateProjectCommand(
    UUID tenantId,
    UUID projectId,
    String name,
    String description
) {
    /**
     * Validates required command state.
     */
    public UpdateProjectCommand {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("projectId must not be null");
        }
    }
}

