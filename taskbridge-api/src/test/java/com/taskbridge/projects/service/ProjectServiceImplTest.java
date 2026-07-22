package com.taskbridge.projects.service;

import com.taskbridge.projects.domain.Project;
import com.taskbridge.projects.domain.ProjectStatus;
import com.taskbridge.projects.repository.ProjectRepository;
import com.taskbridge.projects.service.command.CreateProjectCommand;
import com.taskbridge.projects.service.command.DeleteProjectCommand;
import com.taskbridge.projects.service.command.UpdateProjectCommand;
import com.taskbridge.projects.service.command.UpdateProjectStatusCommand;
import com.taskbridge.projects.service.query.GetProjectsByTeamQuery;
import com.taskbridge.projects.service.result.ProjectView;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private UUID tenantId;
    private UUID teamId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        tenantId  = UUID.randomUUID();
        teamId    = UUID.randomUUID();
        projectId = UUID.randomUUID();
    }

    // ---- create ----------------------------------------------------------

    @Test
    void should_returnProjectResponse_when_projectCreatedSuccessfully() {
        var command = new CreateProjectCommand(tenantId, teamId, "Test Project", "Description", "user");
        var project = Project.create(tenantId, teamId, "Test Project", "Description", "user");
        var view = new ProjectView(projectId, teamId, "Test Project", "Description", ProjectStatus.DRAFT,
            "user", null, null);

        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toView(project)).thenReturn(view);

        ProjectView response = projectService.create(command);

        assertThat(response.name()).isEqualTo("Test Project");
        assertThat(response.status()).isEqualTo(ProjectStatus.DRAFT);
        verify(projectRepository).save(any(Project.class));
        verify(projectMapper).toView(project);
    }

    // ---- update ----------------------------------------------------------

    @Test
    void should_updateFields_when_projectExists() {
        var project = Project.create(tenantId, teamId, "Old Name", "Old Desc", "user");
        var command = new UpdateProjectCommand(tenantId, projectId, "New Name", "New Desc");
        var view = new ProjectView(projectId, teamId, "New Name", "New Desc", ProjectStatus.DRAFT,
            "user", null, null);

        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(projectMapper.toView(project)).thenReturn(view);

        ProjectView response = projectService.update(command);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.description()).isEqualTo("New Desc");
    }

    @Test
    void should_throwEntityNotFound_when_updateCalledForDifferentTenant() {
        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            projectService.update(new UpdateProjectCommand(tenantId, projectId, "X", null))
        ).isInstanceOf(EntityNotFoundException.class);
    }

    // ---- updateStatus ----------------------------------------------------

    @Test
    void should_updateStatus_when_transitionIsValid() {
        var project = Project.create(tenantId, teamId, "P", null, "user");
        var command = new UpdateProjectStatusCommand(tenantId, projectId, ProjectStatus.ACTIVE);
        var view = new ProjectView(projectId, teamId, "P", null, ProjectStatus.ACTIVE,
            "user", null, null);

        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(projectMapper.toView(project)).thenReturn(view);

        ProjectView response = projectService.updateStatus(command);

        assertThat(response.status()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    void should_throwIllegalState_when_statusTransitionIsInvalid() {
        var project = Project.create(tenantId, teamId, "P", null, "user");
        var command = new UpdateProjectStatusCommand(tenantId, projectId, ProjectStatus.COMPLETED);

        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateStatus(command))
            .isInstanceOf(IllegalStateException.class);
    }

    // ---- getByTeam -------------------------------------------------------

    @Test
    void should_returnProjects_when_teamHasProjects() {
        var p1 = Project.create(tenantId, teamId, "P1", null, "user");
        var p2 = Project.create(tenantId, teamId, "P2", null, "user");
        var v1 = new ProjectView(UUID.randomUUID(), teamId, "P1", null, ProjectStatus.DRAFT, "user", null, null);
        var v2 = new ProjectView(UUID.randomUUID(), teamId, "P2", null, ProjectStatus.DRAFT, "user", null, null);

        when(projectRepository.findAllByTenantIdAndTeamIdOrderByCreatedAtDesc(tenantId, teamId))
            .thenReturn(List.of(p1, p2));
        when(projectMapper.toView(p1)).thenReturn(v1);
        when(projectMapper.toView(p2)).thenReturn(v2);

        List<ProjectView> results = projectService.getByTeam(new GetProjectsByTeamQuery(tenantId, teamId));

        assertThat(results).hasSize(2);
        assertThat(results).extracting(ProjectView::name).containsExactly("P1", "P2");
    }

    @Test
    void should_returnEmptyList_when_teamHasNoProjects() {
        when(projectRepository.findAllByTenantIdAndTeamIdOrderByCreatedAtDesc(tenantId, teamId))
            .thenReturn(List.of());

        List<ProjectView> results = projectService.getByTeam(new GetProjectsByTeamQuery(tenantId, teamId));

        assertThat(results).isEmpty();
    }

    // ---- delete ----------------------------------------------------------

    @Test
    void should_deleteProject_when_projectBelongsToTenant() {
        var project = Project.create(tenantId, teamId, "P", null, "user");

        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));

        projectService.delete(new DeleteProjectCommand(tenantId, projectId));

        verify(projectRepository).delete(project);
    }

    @Test
    void should_throwEntityNotFound_when_deletingProjectFromDifferentTenant() {
        UUID otherTenant = UUID.randomUUID();

        when(projectRepository.findByIdAndTenantId(projectId, otherTenant))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(new DeleteProjectCommand(otherTenant, projectId)))
            .isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository, never()).delete(any());
    }

    // ---- cross-tenant isolation ------------------------------------------

    @Test
    void should_neverReturnProjects_when_tenantDoesNotOwnTeam() {
        UUID anotherTenant = UUID.randomUUID();

        when(projectRepository.findAllByTenantIdAndTeamIdOrderByCreatedAtDesc(anotherTenant, teamId))
            .thenReturn(List.of());

        List<ProjectView> results = projectService.getByTeam(new GetProjectsByTeamQuery(anotherTenant, teamId));

        assertThat(results).isEmpty();
        verify(projectRepository).findAllByTenantIdAndTeamIdOrderByCreatedAtDesc(anotherTenant, teamId);
    }

    @Test
    void should_throwIllegalArgument_when_createdByIsBlank() {
        assertThatThrownBy(() -> projectService.create(
            new CreateProjectCommand(tenantId, teamId, "Test Project", "Description", " ")
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("createdBy must not be blank");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void should_throwIllegalArgument_when_teamIdIsNullForQuery() {
        assertThatThrownBy(() -> projectService.getByTeam(
            new GetProjectsByTeamQuery(tenantId, null)
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("teamId must not be null");

        verify(projectRepository, never()).findAllByTenantIdAndTeamIdOrderByCreatedAtDesc(any(), any());
    }
}

