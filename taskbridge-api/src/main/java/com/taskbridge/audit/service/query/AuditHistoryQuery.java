package com.taskbridge.audit.service.query;

import com.taskbridge.audit.domain.AuditEventType;

import java.util.UUID;

/**
 * Application query for retrieving organization-scoped audit history.
 *
 * @param organizationId owning organization identifier
 * @param projectId optional related project identifier
 * @param eventType optional event type filter
 */
public record AuditHistoryQuery(UUID organizationId, UUID projectId, AuditEventType eventType) {
    /**
     * Validates required query state.
     */
    public AuditHistoryQuery {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId must not be null");
        }
    }
}

