package com.taskbridge.projects.service;

import com.taskbridge.projects.domain.Project;
import com.taskbridge.projects.domain.ProjectStatus;
import com.taskbridge.projects.dto.*;
import com.taskbridge.projects.repository.ProjectRepository;
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
        var request = new CreateProjectRequest(teamId, "Test Project", "Description");
        var project = Project.create(tenantId, teamId, "Test Project", "Description", "user");

        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.create(tenantId, "user", request);

        assertThat(response.name()).isEqualTo("Test Project");
        assertThat(response.status()).isEqualTo(ProjectStatus.DRAFT);
        verify(projectRepository).save(any(Project.class));
    }

    // ---- update ----------------------------------------------------------

    @Test
    void should_updateFields_when_projectExists() {
        var project = Project.create(tenantId, teamId, "Old Name", "Old Desc", "user");
        var request = new UpdateProjectRequest("New Name", "New Desc");

        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectResponse response = projectService.update(tenantId, projectId, request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.description()).isEqualTo("New Desc");
    }

    @Test
    void should_throwEntityNotFound_when_updateCalledForDifferentTenant() {
        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            projectService.update(tenantId, projectId, new UpdateProjectRequest("X", null))
        ).isInstanceOf(EntityNotFoundException.class);
    }

    // ---- updateStatus ----------------------------------------------------

    @Test
    void should_updateStatus_when_transitionIsValid() {
        var project = Project.create(tenantId, teamId, "P", null, "user");
        var request = new UpdateProjectStatusRequest(ProjectStatus.ACTIVE);

        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectResponse response = projectService.updateStatus(tenantId, projectId, request);

        assertThat(response.status()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    void should_throwIllegalState_when_statusTransitionIsInvalid() {
        var project = Project.create(tenantId, teamId, "P", null, "user");
        // DRAFT -> COMPLETED is invalid
        var request = new UpdateProjectStatusRequest(ProjectStatus.COMPLETED);

        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateStatus(tenantId, projectId, request))
            .isInstanceOf(IllegalStateException.class);
    }

    // ---- getByTeam -------------------------------------------------------

    @Test
    void should_returnProjects_when_teamHasProjects() {
        var p1 = Project.create(tenantId, teamId, "P1", null, "user");
        var p2 = Project.create(tenantId, teamId, "P2", null, "user");

        when(projectRepository.findAllByTenantIdAndTeamId(tenantId, teamId))
            .thenReturn(List.of(p1, p2));

        List<ProjectResponse> results = projectService.getByTeam(tenantId, teamId);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(ProjectResponse::name).containsExactly("P1", "P2");
    }

    @Test
    void should_returnEmptyList_when_teamHasNoProjects() {
        when(projectRepository.findAllByTenantIdAndTeamId(tenantId, teamId))
            .thenReturn(List.of());

        List<ProjectResponse> results = projectService.getByTeam(tenantId, teamId);

        assertThat(results).isEmpty();
    }

    // ---- delete ----------------------------------------------------------

    @Test
    void should_deleteProject_when_projectBelongsToTenant() {
        var project = Project.create(tenantId, teamId, "P", null, "user");

        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));

        projectService.delete(tenantId, projectId);

        verify(projectRepository).delete(project);
    }

    @Test
    void should_throwEntityNotFound_when_deletingProjectFromDifferentTenant() {
        UUID otherTenant = UUID.randomUUID();

        when(projectRepository.findByIdAndTenantId(projectId, otherTenant))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(otherTenant, projectId))
            .isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository, never()).delete(any());
    }

    // ---- cross-tenant isolation ------------------------------------------

    @Test
    void should_neverReturnProjects_when_tenantDoesNotOwnTeam() {
        UUID anotherTenant = UUID.randomUUID();

        when(projectRepository.findAllByTenantIdAndTeamId(anotherTenant, teamId))
            .thenReturn(List.of());

        List<ProjectResponse> results = projectService.getByTeam(anotherTenant, teamId);

        assertThat(results).isEmpty();
        verify(projectRepository).findAllByTenantIdAndTeamId(anotherTenant, teamId);
    }
}

