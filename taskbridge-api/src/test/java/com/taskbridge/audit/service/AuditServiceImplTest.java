package com.taskbridge.audit.service;

import com.taskbridge.audit.domain.AuditEntry;
import com.taskbridge.audit.domain.AuditEventType;
import com.taskbridge.audit.repository.AuditEntryRepository;
import com.taskbridge.audit.service.command.CreateAuditEntryCommand;
import com.taskbridge.audit.service.query.AuditHistoryQuery;
import com.taskbridge.audit.service.result.AuditEntryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditEntryRepository auditEntryRepository;

    @Mock
    private AuditEntryMapper auditEntryMapper;

    @InjectMocks
    private AuditServiceImpl auditService;

    private UUID organizationId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        projectId = UUID.randomUUID();
    }

    @Test
    void should_createAuditEntry_when_commandIsValid() {
        var command = new CreateAuditEntryCommand(
            organizationId,
            projectId,
            AuditEventType.PROJECT_CREATED,
            null,
            "{\"status\":\"DRAFT\"}",
            "actor@example.com"
        );
        var auditEntry = AuditEntry.create(
            organizationId,
            projectId,
            AuditEventType.PROJECT_CREATED,
            null,
            "{\"status\":\"DRAFT\"}",
            "actor@example.com"
        );
        var view = new AuditEntryView(
            UUID.randomUUID(),
            organizationId,
            projectId,
            AuditEventType.PROJECT_CREATED,
            null,
            "{\"status\":\"DRAFT\"}",
            "actor@example.com",
            null
        );

        when(auditEntryRepository.save(any(AuditEntry.class))).thenReturn(auditEntry);
        when(auditEntryMapper.toView(auditEntry)).thenReturn(view);

        AuditEntryView response = auditService.create(command);

        assertThat(response.organizationId()).isEqualTo(organizationId);
        assertThat(response.eventType()).isEqualTo(AuditEventType.PROJECT_CREATED);
        verify(auditEntryRepository).save(any(AuditEntry.class));
        verify(auditEntryMapper).toView(auditEntry);
    }

    @Test
    void should_returnFilteredHistory_when_eventTypeProvided() {
        var query = new AuditHistoryQuery(organizationId, null, AuditEventType.PROJECT_UPDATED);
        var auditEntry = AuditEntry.create(
            organizationId,
            projectId,
            AuditEventType.PROJECT_UPDATED,
            "{\"name\":\"Old\"}",
            "{\"name\":\"New\"}",
            "actor@example.com"
        );
        var view = new AuditEntryView(
            UUID.randomUUID(),
            organizationId,
            projectId,
            AuditEventType.PROJECT_UPDATED,
            "{\"name\":\"Old\"}",
            "{\"name\":\"New\"}",
            "actor@example.com",
            null
        );

        when(auditEntryRepository.findAllByOrganizationIdAndEventTypeOrderByOccurredAtDesc(
            organizationId, AuditEventType.PROJECT_UPDATED))
            .thenReturn(List.of(auditEntry));
        when(auditEntryMapper.toView(auditEntry)).thenReturn(view);

        List<AuditEntryView> history = auditService.getHistory(query);

        assertThat(history).hasSize(1);
        assertThat(history.getFirst().eventType()).isEqualTo(AuditEventType.PROJECT_UPDATED);
    }

    @Test
    void should_returnFullHistory_when_eventTypeNotProvided() {
        var query = new AuditHistoryQuery(organizationId, null, null);

        when(auditEntryRepository.findAllByOrganizationIdOrderByOccurredAtDesc(organizationId))
            .thenReturn(List.of());

        List<AuditEntryView> history = auditService.getHistory(query);

        assertThat(history).isEmpty();
        verify(auditEntryRepository).findAllByOrganizationIdOrderByOccurredAtDesc(organizationId);
    }

    @Test
    void should_returnProjectHistory_when_projectIdProvided() {
        var query = new AuditHistoryQuery(organizationId, projectId, null);
        var auditEntry = AuditEntry.create(
            organizationId,
            projectId,
            AuditEventType.PROJECT_STATUS_CHANGED,
            "{\"status\":\"DRAFT\"}",
            "{\"status\":\"ACTIVE\"}",
            "actor@example.com"
        );
        var view = new AuditEntryView(
            UUID.randomUUID(),
            organizationId,
            projectId,
            AuditEventType.PROJECT_STATUS_CHANGED,
            "{\"status\":\"DRAFT\"}",
            "{\"status\":\"ACTIVE\"}",
            "actor@example.com",
            null
        );

        when(auditEntryRepository.findAllByOrganizationIdAndProjectIdOrderByOccurredAtDesc(organizationId, projectId))
            .thenReturn(List.of(auditEntry));
        when(auditEntryMapper.toView(auditEntry)).thenReturn(view);

        List<AuditEntryView> history = auditService.getHistory(query);

        assertThat(history).hasSize(1);
        assertThat(history.getFirst().projectId()).isEqualTo(projectId);
    }

    @Test
    void should_throwIllegalArgument_when_createCommandIsNull() {
        assertThatThrownBy(() -> auditService.create(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("command must not be null");

        verify(auditEntryRepository, never()).save(any(AuditEntry.class));
    }
}

