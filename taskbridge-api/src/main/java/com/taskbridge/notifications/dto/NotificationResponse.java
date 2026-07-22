package com.taskbridge.notifications.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * HTTP response payload for recipient-scoped notifications.
 *
 * @param id notification identifier
 * @param projectId related project identifier
 * @param message notification message
 * @param read read status derived from {@code readAt}
 * @param readAt read timestamp when available
 * @param createdAt creation timestamp
 */
public record NotificationResponse(
    UUID id,
    UUID projectId,
    String message,
    boolean read,
    Instant readAt,
    Instant createdAt
) {
}

