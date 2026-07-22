package com.taskbridge.notifications.service;

import com.taskbridge.notifications.service.command.CreateNotificationCommand;
import com.taskbridge.notifications.service.command.DispatchTeamNotificationCommand;
import com.taskbridge.notifications.service.result.NotificationView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamNotificationDispatchServiceTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TeamNotificationDispatchService dispatchService;

    @Test
    void should_dispatchEqualNotificationsToAllTeamMembers() {
        UUID organizationId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        List<String> teamMembers = List.of("alice@example.com", "bob@example.com", "carol@example.com");

        when(notificationService.create(any(CreateNotificationCommand.class))).thenReturn(
            new NotificationView(UUID.randomUUID(), organizationId, "user", projectId, "msg", null, Instant.now())
        );

        int dispatched = dispatchService.dispatchToTeamMembers(new DispatchTeamNotificationCommand(
            organizationId,
            projectId,
            "Milestone reopened",
            teamMembers
        ));

        assertThat(dispatched).isEqualTo(teamMembers.size());

        ArgumentCaptor<CreateNotificationCommand> captor = ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(notificationService, times(teamMembers.size())).create(captor.capture());

        List<CreateNotificationCommand> sent = captor.getAllValues();
        assertThat(sent).extracting(CreateNotificationCommand::organizationId).containsOnly(organizationId);
        assertThat(sent).extracting(CreateNotificationCommand::projectId).containsOnly(projectId);
        assertThat(sent).extracting(CreateNotificationCommand::message).containsOnly("Milestone reopened");
        assertThat(sent).extracting(CreateNotificationCommand::recipient).containsExactlyElementsOf(teamMembers);
    }

    @Test
    void should_throwIllegalArgument_when_dispatchCommandIsNull() {
        assertThatThrownBy(() -> dispatchService.dispatchToTeamMembers(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("command must not be null");
    }
}

