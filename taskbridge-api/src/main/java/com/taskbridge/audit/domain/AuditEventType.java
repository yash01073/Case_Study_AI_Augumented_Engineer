package com.taskbridge.audit.domain;

/**
 * Supported audit event types for immutable domain activity tracking.
 */
public enum AuditEventType {
    PROJECT_CREATED,
    PROJECT_UPDATED,
    PROJECT_STATUS_CHANGED,
    PROJECT_DELETED,
    NOTIFICATION_CREATED,
    NOTIFICATION_READ
}

