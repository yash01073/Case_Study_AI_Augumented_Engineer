package com.taskbridge.projects.domain;

/**
 * Lifecycle states for a Project.
 * Allowed transitions:
 *   DRAFT -> ACTIVE
 *   ACTIVE -> ON_HOLD | COMPLETED | CANCELLED
 *   ON_HOLD -> ACTIVE | CANCELLED
 *   COMPLETED -> ARCHIVED
 *   CANCELLED -> (terminal)
 *   ARCHIVED -> (terminal)
 */
public enum ProjectStatus {
    DRAFT,
    ACTIVE,
    ON_HOLD,
    COMPLETED,
    ARCHIVED,
    CANCELLED
}

