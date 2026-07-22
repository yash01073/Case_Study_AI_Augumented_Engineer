package com.taskbridge.audit.service;

import com.taskbridge.audit.service.command.CreateAuditEntryCommand;
import com.taskbridge.audit.service.query.AuditHistoryQuery;
import com.taskbridge.audit.service.result.AuditEntryView;

import java.util.List;

/**
 * Application service for immutable audit entry creation and retrieval.
 */
public interface AuditService {

    /**
     * Creates and persists an immutable audit entry.
     *
     * @param command validated audit create command
     * @return persisted audit entry as an application view
     */
    AuditEntryView create(CreateAuditEntryCommand command);

    /**
     * Retrieves organization-scoped audit history.
     *
     * @param query validated audit history query
     * @return immutable audit history ordered by occurrence descending
     */
    List<AuditEntryView> getHistory(AuditHistoryQuery query);
}

