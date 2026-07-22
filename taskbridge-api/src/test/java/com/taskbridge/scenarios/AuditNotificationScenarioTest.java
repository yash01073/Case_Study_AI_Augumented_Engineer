package com.taskbridge.scenarios;

import com.taskbridge.audit.domain.AuditEntry;
import com.taskbridge.audit.domain.AuditEventType;
import com.taskbridge.audit.repository.AuditEntryRepository;
import com.taskbridge.audit.service.AuditEntryMapper;
import com.taskbridge.audit.service.AuditServiceImpl;
import com.taskbridge.audit.service.command.CreateAuditEntryCommand;
import com.taskbridge.audit.service.query.AuditHistoryQuery;
import com.taskbridge.audit.service.result.AuditEntryView;
import com.taskbridge.notifications.domain.Notification;
import com.taskbridge.notifications.repository.NotificationRepository;
import com.taskbridge.notifications.service.NotificationMapper;
import com.taskbridge.notifications.service.NotificationService;
import com.taskbridge.notifications.service.NotificationServiceImpl;
import com.taskbridge.notifications.service.TeamNotificationDispatchService;
import com.taskbridge.notifications.service.command.CreateNotificationCommand;
import com.taskbridge.notifications.service.command.DispatchTeamNotificationCommand;
import com.taskbridge.notifications.service.command.MarkNotificationReadCommand;
import com.taskbridge.notifications.service.result.NotificationView;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Scenario-driven Mockito coverage for audit and notification behaviors.
 */
@ExtendWith(MockitoExtension.class)
class AuditNotificationScenarioTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditEntryRepository auditEntryRepository;

    @Mock
    private AuditEntryMapper auditEntryMapper;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    private TeamNotificationDispatchService dispatchService;
    private AuditServiceImpl auditService;
    private NotificationServiceImpl notificationServiceImpl;

    @BeforeEach
    void setUp() {
        dispatchService = new TeamNotificationDispatchService(notificationService);
        auditService = new AuditServiceImpl(auditEntryRepository, auditEntryMapper);
        notificationServiceImpl = new NotificationServiceImpl(notificationRepository, notificationMapper);
    }

    @Test
    void should_dispatch_equal_notification_to_all_team_members() {
        UUID organizationId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        List<String> teamMembers = List.of("alice@example.com", "bob@example.com", "carol@example.com");

        when(notificationService.create(any(CreateNotificationCommand.class))).thenReturn(
            new NotificationView(UUID.randomUUID(), organizationId, "user", projectId, "Milestone reopened", null, Instant.now())
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
    void should_create_audit_entry() {
        UUID organizationId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        CreateAuditEntryCommand command = new CreateAuditEntryCommand(
            organizationId,
            projectId,
            AuditEventType.PROJECT_UPDATED,
            "{\"status\":\"DRAFT\"}",
            "{\"status\":\"ACTIVE\"}",
            "actor@example.com"
        );

        AuditEntry auditEntry = AuditEntry.create(
            organizationId,
            projectId,
            AuditEventType.PROJECT_UPDATED,
            "{\"status\":\"DRAFT\"}",
            "{\"status\":\"ACTIVE\"}",
            "actor@example.com"
        );

        AuditEntryView view = new AuditEntryView(
            UUID.randomUUID(),
            organizationId,
            projectId,
            AuditEventType.PROJECT_UPDATED,
            "{\"status\":\"DRAFT\"}",
            "{\"status\":\"ACTIVE\"}",
            "actor@example.com",
            Instant.now()
        );

        when(auditEntryRepository.save(any(AuditEntry.class))).thenReturn(auditEntry);
        when(auditEntryMapper.toView(auditEntry)).thenReturn(view);

        AuditEntryView result = auditService.create(command);

        assertThat(result.organizationId()).isEqualTo(organizationId);
        assertThat(result.projectId()).isEqualTo(projectId);
        assertThat(result.eventType()).isEqualTo(AuditEventType.PROJECT_UPDATED);
        verify(auditEntryRepository).save(any(AuditEntry.class));
    }

    @Test
    void should_preserve_audit_immutability_when_querying_history() {
        UUID organizationId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        AuditEntry auditEntry = AuditEntry.create(
            organizationId,
            projectId,
            AuditEventType.PROJECT_CREATED,
            null,
            "{\"name\":\"Alpha\"}",
            "actor@example.com"
        );
        AuditEntryView view = new AuditEntryView(
            UUID.randomUUID(),
            organizationId,
            projectId,
            AuditEventType.PROJECT_CREATED,
            null,
            "{\"name\":\"Alpha\"}",
            "actor@example.com",
            Instant.now()
        );

        when(auditEntryRepository.findAllByOrganizationIdAndProjectIdOrderByOccurredAtDesc(organizationId, projectId))
            .thenReturn(List.of(auditEntry));
        when(auditEntryMapper.toView(auditEntry)).thenReturn(view);

        List<AuditEntryView> history = auditService.getHistory(
            new AuditHistoryQuery(organizationId, projectId, null, null, null)
        );

        assertThat(history).hasSize(1);
        verify(auditEntryRepository, never()).save(any(AuditEntry.class));
    }

    @Test
    void should_apply_date_range_filtering() {
        UUID organizationId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Instant from = Instant.parse("2026-03-01T00:00:00Z");
        Instant to = Instant.parse("2026-03-31T23:59:59Z");

        AuditEntry auditEntry = AuditEntry.create(
            organizationId,
            projectId,
            AuditEventType.PROJECT_STATUS_CHANGED,
            "{\"status\":\"DRAFT\"}",
            "{\"status\":\"ACTIVE\"}",
            "actor@example.com"
        );
        AuditEntryView view = new AuditEntryView(
            UUID.randomUUID(),
            organizationId,
            projectId,
            AuditEventType.PROJECT_STATUS_CHANGED,
            "{\"status\":\"DRAFT\"}",
            "{\"status\":\"ACTIVE\"}",
            "actor@example.com",
            Instant.now()
        );

        when(auditEntryRepository.findAllByOrganizationIdAndOccurredAtBetweenOrderByOccurredAtDesc(organizationId, from, to))
            .thenReturn(List.of(auditEntry));
        when(auditEntryMapper.toView(auditEntry)).thenReturn(view);

        List<AuditEntryView> history = auditService.getHistory(
            new AuditHistoryQuery(organizationId, null, null, from, to)
        );

        assertThat(history).hasSize(1);
        verify(auditEntryRepository).findAllByOrganizationIdAndOccurredAtBetweenOrderByOccurredAtDesc(organizationId, from, to);
    }

    @Test
    void should_apply_event_filtering() {
        UUID organizationId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        AuditEntry auditEntry = AuditEntry.create(
            organizationId,
            projectId,
            AuditEventType.PROJECT_DELETED,
            "{\"deleted\":false}",
            "{\"deleted\":true}",
            "actor@example.com"
        );
        AuditEntryView view = new AuditEntryView(
            UUID.randomUUID(),
            organizationId,
            projectId,
            AuditEventType.PROJECT_DELETED,
            "{\"deleted\":false}",
            "{\"deleted\":true}",
            "actor@example.com",
            Instant.now()
        );

        when(auditEntryRepository.findAllByOrganizationIdAndEventTypeOrderByOccurredAtDesc(
            organizationId,
            AuditEventType.PROJECT_DELETED
        )).thenReturn(List.of(auditEntry));
        when(auditEntryMapper.toView(auditEntry)).thenReturn(view);

        List<AuditEntryView> history = auditService.getHistory(
            new AuditHistoryQuery(organizationId, null, AuditEventType.PROJECT_DELETED, null, null)
        );

        assertThat(history).hasSize(1);
        assertThat(history.getFirst().eventType()).isEqualTo(AuditEventType.PROJECT_DELETED);
    }

    @Test
    void should_block_unauthorized_organization_access_when_marking_notification_read() {
        UUID requestedOrganization = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findByIdAndOrganizationId(notificationId, requestedOrganization))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationServiceImpl.markAsRead(
            new MarkNotificationReadCommand(requestedOrganization, notificationId, "user@example.com")
        )).isInstanceOf(EntityNotFoundException.class);

        verify(notificationRepository, never()).save(any(Notification.class));
    }
}

