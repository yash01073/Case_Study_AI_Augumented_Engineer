package com.taskbridge.projects.service;

import com.taskbridge.projects.service.command.CreateProjectCommand;
import com.taskbridge.projects.service.command.DeleteProjectCommand;
import com.taskbridge.projects.service.command.UpdateProjectCommand;
import com.taskbridge.projects.service.command.UpdateProjectStatusCommand;
import com.taskbridge.projects.service.query.GetProjectsByTeamQuery;
import com.taskbridge.projects.service.result.ProjectView;

import java.util.List;

/**
 * Application-level contract for project operations.
 * <p>
 * This service is transport-neutral: callers provide validated application
 * commands and queries, and receive immutable application views.
 * </p>
 */
public interface ProjectService {

    /**
     * Creates a new project.
     *
     * @param command validated create command
     * @return the persisted project as an application view
     */
    ProjectView create(CreateProjectCommand command);

    /**
     * Updates mutable fields of an existing project.
     *
     * @param command validated update command
     * @return updated project as an application view
     */
    ProjectView update(UpdateProjectCommand command);

    /**
     * Transitions a project to a new lifecycle status.
     *
     * @param command validated status transition command
     * @return updated project as an application view
     */
    ProjectView updateStatus(UpdateProjectStatusCommand command);

    /**
     * Returns all projects belonging to the given team, scoped to tenant.
     *
     * @param query validated team query
     * @return list of application views (may be empty)
     */
    List<ProjectView> getByTeam(GetProjectsByTeamQuery query);

    /**
     * Permanently deletes a project. Validates tenant ownership before deletion.
     *
     * @param command validated delete command
     */
    void delete(DeleteProjectCommand command);
}

