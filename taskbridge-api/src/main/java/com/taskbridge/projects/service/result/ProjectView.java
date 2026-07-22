package com.taskbridge.projects.service.result;

import com.taskbridge.projects.domain.ProjectStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Transport-neutral application view of a project.
 *
 * @param id          project identifier
 * @param teamId      owning team identifier
 * @param name        project display name
 * @param description optional project description
 * @param status      lifecycle status
 * @param createdBy   creator identifier
 * @param createdAt   creation timestamp
 * @param updatedAt   last modification timestamp
 */
public record ProjectView(
    UUID id,
    UUID teamId,
    String name,
    String description,
    ProjectStatus status,
    String createdBy,
    Instant createdAt,
    Instant updatedAt
) {
}

