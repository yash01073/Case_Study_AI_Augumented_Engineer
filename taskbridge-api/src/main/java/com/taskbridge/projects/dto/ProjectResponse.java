package com.taskbridge.projects.dto;

import com.taskbridge.projects.domain.Project;
import com.taskbridge.projects.domain.ProjectStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Output DTO for project data. Never exposes the JPA entity directly.
 */
public record ProjectResponse(
    UUID id,
    UUID teamId,
    String name,
    String description,
    ProjectStatus status,
    String createdBy,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
            project.getId(),
            project.getTeamId(),
            project.getName(),
            project.getDescription(),
            project.getStatus(),
            project.getCreatedBy(),
            project.getCreatedAt(),
            project.getUpdatedAt()
        );
    }
}

