package com.taskbridge.projects.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ProjectTest {

    private static Project newDraftProject() {
        return Project.create(
            UUID.randomUUID(), UUID.randomUUID(),
            "Test Project", "Description", "user@example.com"
        );
    }

    @Test
    void should_setStatusToDraft_when_projectCreated() {
        Project project = newDraftProject();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.DRAFT);
    }

    @Test
    void should_transitionToActive_when_draftProjectActivated() {
        Project project = newDraftProject();
        project.updateStatus(ProjectStatus.ACTIVE);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"ON_HOLD", "COMPLETED", "CANCELLED"})
    void should_throwException_when_transitioningFromDraftToInvalidStatus(ProjectStatus invalidTarget) {
        Project project = newDraftProject();
        assertThatThrownBy(() -> project.updateStatus(invalidTarget))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("DRAFT");
    }

    @Test
    void should_transitionToCompleted_when_activeProjectCompleted() {
        Project project = newDraftProject();
        project.updateStatus(ProjectStatus.ACTIVE);
        project.updateStatus(ProjectStatus.COMPLETED);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    void should_throwException_when_transitioningFromTerminalState() {
        Project project = newDraftProject();
        project.updateStatus(ProjectStatus.ACTIVE);
        project.updateStatus(ProjectStatus.CANCELLED);
        assertThatThrownBy(() -> project.updateStatus(ProjectStatus.DRAFT))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_updateNameAndDescription_when_updateCalled() {
        Project project = newDraftProject();
        project.update("New Name", "New Description");
        assertThat(project.getName()).isEqualTo("New Name");
        assertThat(project.getDescription()).isEqualTo("New Description");
    }

    @Test
    void should_setTenantId_when_projectCreated() {
        UUID tenantId = UUID.randomUUID();
        Project project = Project.create(tenantId, UUID.randomUUID(), "P", null, "u");
        assertThat(project.getTenantId()).isEqualTo(tenantId);
    }
}

