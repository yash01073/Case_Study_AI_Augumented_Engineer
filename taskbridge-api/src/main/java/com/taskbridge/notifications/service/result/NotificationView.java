package com.taskbridge.notifications.service.result;

import java.time.Instant;
import java.util.UUID;

/**
 * Transport-neutral application view of a notification.
 *
 * @param id notification identifier
 * @param organizationId owning organization identifier
 * @param recipient recipient identifier
 * @param projectId related project identifier
 * @param message notification message
 * @param readAt read timestamp, {@code null} when unread
 * @param createdAt creation timestamp
 */
public record NotificationView(
    UUID id,
    UUID organizationId,
    String recipient,
    UUID projectId,
    String message,
    Instant readAt,
    Instant createdAt
) {
    /**
     * Indicates whether the notification has been read.
     *
     * @return {@code true} when {@code readAt} is set
     */
    public boolean isRead() {
        return readAt != null;
    }
}

