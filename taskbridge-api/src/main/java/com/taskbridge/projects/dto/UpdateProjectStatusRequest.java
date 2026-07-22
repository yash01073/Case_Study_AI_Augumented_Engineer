package com.taskbridge.projects.dto;

import com.taskbridge.projects.domain.ProjectStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Input DTO for transitioning project status.
 */
public record UpdateProjectStatusRequest(

    @NotNull(message = "status is required")
    ProjectStatus status
) {}

