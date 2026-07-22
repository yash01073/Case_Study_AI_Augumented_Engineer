package com.taskbridge.notifications.service.query;

import java.util.UUID;

/**
 * Application query for retrieving recipient-scoped notifications.
 *
 * @param organizationId owning organization identifier
 * @param recipient recipient identifier
 * @param unreadOnly whether only unread notifications should be returned
 */
public record NotificationQuery(UUID organizationId, String recipient, boolean unreadOnly) {
    /**
     * Validates required query state.
     */
    public NotificationQuery {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId must not be null");
        }
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException("recipient must not be blank");
        }
    }
}

