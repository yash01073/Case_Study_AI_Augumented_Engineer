package com.taskbridge.audit.service;

import com.taskbridge.audit.domain.AuditEntry;
import com.taskbridge.audit.service.result.AuditEntryView;
import org.springframework.stereotype.Component;

/**
 * Maps immutable audit entities to transport-neutral application views.
 */
@Component
public class AuditEntryMapper {

    /**
     * Converts an immutable audit entry into an application view.
     *
     * @param auditEntry domain audit entry
     * @return immutable application view
     */
    public AuditEntryView toView(AuditEntry auditEntry) {
        return new AuditEntryView(
            auditEntry.getId(),
            auditEntry.getOrganizationId(),
            auditEntry.getProjectId(),
            auditEntry.getEventType(),
            auditEntry.getPreviousState(),
            auditEntry.getNewState(),
            auditEntry.getActor(),
            auditEntry.getOccurredAt()
        );
    }
}

