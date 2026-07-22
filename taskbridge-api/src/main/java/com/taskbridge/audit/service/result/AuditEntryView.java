package com.taskbridge.audit.service.result;

import com.taskbridge.audit.domain.AuditEventType;

import java.time.Instant;
import java.util.UUID;

/**
 * Transport-neutral application view of an immutable audit entry.
 *
 * @param id audit entry identifier
 * @param organizationId owning organization identifier
 * @param projectId related project identifier
 * @param eventType business event type
 * @param previousState serialized previous state snapshot
 * @param newState serialized new state snapshot
 * @param actor authenticated actor identifier
 * @param occurredAt occurrence timestamp
 */
public record AuditEntryView(
    UUID id,
    UUID organizationId,
    UUID projectId,
    AuditEventType eventType,
    String previousState,
    String newState,
    String actor,
    Instant occurredAt
) {
}

