package com.taskbridge.notifications.service.command;

import java.util.UUID;

/**
 * Application command for marking a notification as read.
 *
 * @param organizationId owning organization identifier
 * @param notificationId target notification identifier
 * @param recipient authenticated recipient identifier
 */
public record MarkNotificationReadCommand(UUID organizationId, UUID notificationId, String recipient) {
    /**
     * Validates required command state.
     */
    public MarkNotificationReadCommand {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId must not be null");
        }
        if (notificationId == null) {
            throw new IllegalArgumentException("notificationId must not be null");
        }
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException("recipient must not be blank");
        }
    }
}

