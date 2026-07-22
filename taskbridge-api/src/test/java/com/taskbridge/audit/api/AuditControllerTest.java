package com.taskbridge.audit.api;

import com.taskbridge.audit.domain.AuditEventType;
import com.taskbridge.audit.dto.AuditEntryResponse;
import com.taskbridge.audit.dto.CreateAuditEntryRequest;
import com.taskbridge.audit.service.AuditService;
import com.taskbridge.audit.service.command.CreateAuditEntryCommand;
import com.taskbridge.audit.service.result.AuditEntryView;
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

class AuditControllerTest {

    private final AuditService auditService = mock(AuditService.class);
    private final AuditApiMapper auditApiMapper = new AuditApiMapper();
    private final AuditController auditController = new AuditController(auditService, auditApiMapper);

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void should_createAuditEntry_when_requestIsValid() {
        UUID organizationId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TenantContext.set(organizationId, "actor@example.com");

        CreateAuditEntryRequest request = new CreateAuditEntryRequest(
            projectId,
            AuditEventType.PROJECT_CREATED,
            null,
            "{\"status\":\"DRAFT\"}"
        );
        AuditEntryView view = new AuditEntryView(
            UUID.randomUUID(),
            organizationId,
            projectId,
            AuditEventType.PROJECT_CREATED,
            null,
            "{\"status\":\"DRAFT\"}",
            "actor@example.com",
            Instant.now()
        );

        when(auditService.create(any(CreateAuditEntryCommand.class))).thenReturn(view);

        ResponseEntity<AuditEntryResponse> response = auditController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().projectId()).isEqualTo(projectId);

        ArgumentCaptor<CreateAuditEntryCommand> captor = ArgumentCaptor.forClass(CreateAuditEntryCommand.class);
        verify(auditService).create(captor.capture());
        assertThat(captor.getValue().organizationId()).isEqualTo(organizationId);
        assertThat(captor.getValue().actor()).isEqualTo("actor@example.com");
    }

    @Test
    void should_returnProjectHistory_when_projectIdProvided() {
        UUID organizationId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TenantContext.set(organizationId, "actor@example.com");

        AuditEntryView view = new AuditEntryView(
            UUID.randomUUID(),
            organizationId,
            projectId,
            AuditEventType.PROJECT_UPDATED,
            "{\"name\":\"Old\"}",
            "{\"name\":\"New\"}",
            "actor@example.com",
            Instant.now()
        );

        when(auditService.getHistory(any())).thenReturn(List.of(view));

        ResponseEntity<List<AuditEntryResponse>> response = auditController.getByProject(projectId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().projectId()).isEqualTo(projectId);
    }
}

