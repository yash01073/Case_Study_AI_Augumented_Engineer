package com.taskbridge.notifications.service;

import com.taskbridge.notifications.domain.Notification;
import com.taskbridge.notifications.repository.NotificationRepository;
import com.taskbridge.notifications.service.command.CreateNotificationCommand;
import com.taskbridge.notifications.service.command.MarkNotificationReadCommand;
import com.taskbridge.notifications.service.query.NotificationQuery;
import com.taskbridge.notifications.service.result.NotificationView;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID organizationId;
    private UUID notificationId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        projectId = UUID.randomUUID();
    }

    @Test
    void should_createNotification_when_commandIsValid() {
        var command = new CreateNotificationCommand(
            organizationId,
            "recipient@example.com",
            projectId,
            "Project created"
        );
        var notification = Notification.create(
            organizationId,
            "recipient@example.com",
            projectId,
            "Project created"
        );
        var view = new NotificationView(
            notificationId,
            organizationId,
            "recipient@example.com",
            projectId,
            "Project created",
            null,
            null
        );

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toView(notification)).thenReturn(view);

        NotificationView response = notificationService.create(command);

        assertThat(response.organizationId()).isEqualTo(organizationId);
        assertThat(response.isRead()).isFalse();
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void should_markNotificationRead_when_notificationBelongsToOrganization() {
        var command = new MarkNotificationReadCommand(organizationId, notificationId, "recipient@example.com");
        var notification = Notification.create(
            organizationId,
            "recipient@example.com",
            projectId,
            "Project updated"
        );
        notification.markAsRead();
        var view = new NotificationView(
            notificationId,
            organizationId,
            "recipient@example.com",
            projectId,
            "Project updated",
            notification.getReadAt(),
            null
        );

        when(notificationRepository.findByIdAndOrganizationId(notificationId, organizationId))
            .thenReturn(Optional.of(Notification.create(
                organizationId,
                "recipient@example.com",
                projectId,
                "Project updated"
            )));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notificationMapper.toView(any(Notification.class))).thenReturn(view);

        NotificationView response = notificationService.markAsRead(command);

        assertThat(response.isRead()).isTrue();
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void should_throwNotFound_when_notificationBelongsToDifferentOrganization() {
        when(notificationRepository.findByIdAndOrganizationId(notificationId, organizationId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(
            new MarkNotificationReadCommand(organizationId, notificationId, "recipient@example.com")
        )).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void should_throwNotFound_when_notificationRecipientDoesNotMatchCurrentUser() {
        Notification notification = Notification.create(
            organizationId,
            "actual@example.com",
            projectId,
            "Project updated"
        );

        when(notificationRepository.findByIdAndOrganizationId(notificationId, organizationId))
            .thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(
            new MarkNotificationReadCommand(organizationId, notificationId, "other@example.com")
        )).isInstanceOf(EntityNotFoundException.class);

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void should_returnUnreadNotifications_when_unreadOnlyRequested() {
        var query = new NotificationQuery(organizationId, "recipient@example.com", true);
        var notification = Notification.create(
            organizationId,
            "recipient@example.com",
            projectId,
            "Project created"
        );
        var view = new NotificationView(
            notificationId,
            organizationId,
            "recipient@example.com",
            projectId,
            "Project created",
            null,
            null
        );

        when(notificationRepository.findAllByOrganizationIdAndRecipientAndReadAtIsNullOrderByCreatedAtDesc(
            organizationId, "recipient@example.com"))
            .thenReturn(List.of(notification));
        when(notificationMapper.toView(notification)).thenReturn(view);

        List<NotificationView> results = notificationService.getNotifications(query);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().isRead()).isFalse();
    }

    @Test
    void should_throwIllegalArgument_when_notificationCommandIsNull() {
        assertThatThrownBy(() -> notificationService.create(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("command must not be null");

        verify(notificationRepository, never()).save(any(Notification.class));
    }
}

