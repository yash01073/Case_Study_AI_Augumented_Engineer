package com.taskbridge.projects.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Input DTO for updating mutable project fields.
 */
public record UpdateProjectRequest(

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must not exceed 255 characters")
    String name,

    @Size(max = 2000, message = "description must not exceed 2000 characters")
    String description
) {}

