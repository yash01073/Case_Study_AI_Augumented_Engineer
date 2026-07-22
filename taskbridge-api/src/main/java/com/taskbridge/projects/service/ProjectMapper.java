package com.taskbridge.projects.service;

import com.taskbridge.projects.domain.Project;
import com.taskbridge.projects.service.result.ProjectView;
import org.springframework.stereotype.Component;

/**
 * Maps domain {@link Project} aggregates to transport-neutral application views.
 */
@Component
public class ProjectMapper {

    /**
     * Converts a domain aggregate into an application view.
     *
     * @param project domain aggregate to convert
     * @return immutable application view
     */
    public ProjectView toView(Project project) {
        return new ProjectView(
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

