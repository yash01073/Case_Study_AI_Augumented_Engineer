package com.taskbridge.audit.dto;

import com.taskbridge.audit.domain.AuditEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * HTTP request payload for creating an immutable audit entry.
 * <p>
 * Tenant and actor identity are derived from the authenticated request context,
 * not from this payload.
 * </p>
 *
 * @param projectId related project identifier
 * @param eventType business event type
 * @param previousState serialized previous state snapshot
 * @param newState serialized new state snapshot
 */
public record CreateAuditEntryRequest(
    @NotNull(message = "projectId is required")
    UUID projectId,

    @NotNull(message = "eventType is required")
    AuditEventType eventType,

    @Size(max = 20000, message = "previousState must not exceed 20000 characters")
    String previousState,

    @Size(max = 20000, message = "newState must not exceed 20000 characters")
    String newState
) {
}

