package com.taskbridge.audit.service.command;

import com.taskbridge.audit.domain.AuditEventType;

import java.util.UUID;

/**
 * Application command for creating an immutable audit entry.
 *
 * @param organizationId owning organization identifier
 * @param projectId related project identifier
 * @param eventType business event type
 * @param previousState serialized previous state snapshot
 * @param newState serialized new state snapshot
 * @param actor authenticated actor identifier
 */
public record CreateAuditEntryCommand(
    UUID organizationId,
    UUID projectId,
    AuditEventType eventType,
    String previousState,
    String newState,
    String actor
) {
    /**
     * Validates required command state.
     */
    public CreateAuditEntryCommand {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId must not be null");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("projectId must not be null");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("eventType must not be null");
        }
        if (actor == null || actor.trim().isEmpty()) {
            throw new IllegalArgumentException("actor must not be blank");
        }
    }
}

