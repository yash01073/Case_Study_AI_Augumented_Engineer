package com.taskbridge.projects.service.command;

import java.util.UUID;

/**
 * Application command for creating a project.
 * <p>
 * This command is transport-neutral and can be created by HTTP controllers,
 * event handlers, or future batch processes.
 * </p>
 *
 * @param tenantId    owning tenant identifier
 * @param teamId      owning team identifier within the tenant
 * @param name        project display name
 * @param description optional project description
 * @param createdBy   authenticated actor identifier
 */
public record CreateProjectCommand(
    UUID tenantId,
    UUID teamId,
    String name,
    String description,
    String createdBy
) {
    /**
     * Validates required command state.
     */
    public CreateProjectCommand {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        if (teamId == null) {
            throw new IllegalArgumentException("teamId must not be null");
        }
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("createdBy must not be blank");
        }
    }
}

