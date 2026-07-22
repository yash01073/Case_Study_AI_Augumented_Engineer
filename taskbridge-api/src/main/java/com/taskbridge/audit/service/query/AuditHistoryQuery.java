package com.taskbridge.audit.service.query;

import com.taskbridge.audit.domain.AuditEventType;

import java.time.Instant;
import java.util.UUID;

/**
 * Application query for retrieving organization-scoped audit history.
 *
 * @param organizationId owning organization identifier
 * @param projectId optional related project identifier
 * @param eventType optional event type filter
 * @param from optional start timestamp (inclusive)
 * @param to optional end timestamp (inclusive)
 */
public record AuditHistoryQuery(
    UUID organizationId,
    UUID projectId,
    AuditEventType eventType,
    Instant from,
    Instant to
) {
    /**
     * Validates required query state.
     */
    public AuditHistoryQuery {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId must not be null");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }
    }
}

