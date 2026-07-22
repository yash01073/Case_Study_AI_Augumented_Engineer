package com.taskbridge.projects.service.command;

import java.util.UUID;

/**
 * Application command for deleting a project.
 *
 * @param tenantId  owning tenant identifier
 * @param projectId target project identifier
 */
public record DeleteProjectCommand(UUID tenantId, UUID projectId) {
    /**
     * Validates required command state.
     */
    public DeleteProjectCommand {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("projectId must not be null");
        }
    }
}


