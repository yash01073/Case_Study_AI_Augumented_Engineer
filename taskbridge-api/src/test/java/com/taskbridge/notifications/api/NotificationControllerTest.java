package com.taskbridge.notifications.api;

import com.taskbridge.notifications.dto.NotificationResponse;
import com.taskbridge.notifications.service.NotificationService;
import com.taskbridge.notifications.service.command.MarkNotificationReadCommand;
import com.taskbridge.notifications.service.result.NotificationView;
import com.taskbridge.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationControllerTest {

    private final NotificationService notificationService = mock(NotificationService.class);
    private final NotificationApiMapper notificationApiMapper = new NotificationApiMapper();
    private final NotificationController notificationController = new NotificationController(notificationService, notificationApiMapper);

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void should_returnNotificationsForUser_when_requestIsValid() {
        UUID organizationId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TenantContext.set(organizationId, "user@example.com");

        NotificationView view = new NotificationView(
            notificationId,
            organizationId,
            "user@example.com",
            projectId,
            "Project created",
            null,
            Instant.now()
        );

        when(notificationService.getNotifications(any())).thenReturn(List.of(view));

        ResponseEntity<List<NotificationResponse>> response = notificationController.getNotifications(
            "user@example.com",
            true
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().read()).isFalse();
    }

    @Test
    void should_markNotificationRead_forAuthenticatedRecipient() {
        UUID organizationId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TenantContext.set(organizationId, "user@example.com");

        NotificationView view = new NotificationView(
            notificationId,
            organizationId,
            "user@example.com",
            projectId,
            "Project updated",
            Instant.now(),
            Instant.now()
        );

        when(notificationService.markAsRead(any(MarkNotificationReadCommand.class))).thenReturn(view);

        ResponseEntity<NotificationResponse> response = notificationController.markAsRead(notificationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().read()).isTrue();

        ArgumentCaptor<MarkNotificationReadCommand> captor = ArgumentCaptor.forClass(MarkNotificationReadCommand.class);
        verify(notificationService).markAsRead(captor.capture());
        assertThat(captor.getValue().organizationId()).isEqualTo(organizationId);
        assertThat(captor.getValue().recipient()).isEqualTo("user@example.com");
    }
}

