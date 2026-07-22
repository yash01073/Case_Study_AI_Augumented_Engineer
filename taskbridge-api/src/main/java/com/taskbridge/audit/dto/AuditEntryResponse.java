package com.taskbridge.audit.dto;

import com.taskbridge.audit.domain.AuditEventType;

import java.time.Instant;
import java.util.UUID;

/**
 * HTTP response payload for immutable audit history.
 *
 * @param id audit entry identifier
 * @param projectId related project identifier
 * @param eventType business event type
 * @param previousState serialized previous state snapshot
 * @param newState serialized new state snapshot
 * @param actor authenticated actor identifier
 * @param occurredAt occurrence timestamp
 */
public record AuditEntryResponse(
    UUID id,
    UUID projectId,
    AuditEventType eventType,
    String previousState,
    String newState,
    String actor,
    Instant occurredAt
) {
}

