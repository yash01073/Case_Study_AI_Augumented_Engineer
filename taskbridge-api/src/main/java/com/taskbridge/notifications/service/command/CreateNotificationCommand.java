package com.taskbridge.notifications.service.command;

import java.util.UUID;

/**
 * Application command for creating a tenant-scoped notification.
 *
 * @param organizationId owning organization identifier
 * @param recipient recipient identifier
 * @param projectId related project identifier
 * @param message notification message
 */
public record CreateNotificationCommand(
    UUID organizationId,
    String recipient,
    UUID projectId,
    String message
) {
    /**
     * Validates required command state.
     */
    public CreateNotificationCommand {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId must not be null");
        }
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException("recipient must not be blank");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("projectId must not be null");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}

