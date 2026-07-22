package com.taskbridge.audit.service;

import com.taskbridge.audit.domain.AuditEntry;
import com.taskbridge.audit.repository.AuditEntryRepository;
import com.taskbridge.audit.service.command.CreateAuditEntryCommand;
import com.taskbridge.audit.service.query.AuditHistoryQuery;
import com.taskbridge.audit.service.result.AuditEntryView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default application service for immutable audit entry workflows.
 */
@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditEntryRepository auditEntryRepository;
    private final AuditEntryMapper auditEntryMapper;

    public AuditServiceImpl(AuditEntryRepository auditEntryRepository, AuditEntryMapper auditEntryMapper) {
        this.auditEntryRepository = auditEntryRepository;
        this.auditEntryMapper = auditEntryMapper;
    }

    @Override
    public AuditEntryView create(CreateAuditEntryCommand command) {
        validate(command);

        log.info("Audit action=create outcome=attempt organization={} eventType={} actor={}",
            command.organizationId(), command.eventType(), command.actor());

        AuditEntry auditEntry = AuditEntry.create(
            command.organizationId(),
            command.projectId(),
            command.eventType(),
            command.previousState(),
            command.newState(),
            command.actor()
        );

        AuditEntry saved = auditEntryRepository.save(auditEntry);

        log.info("Audit action=create outcome=success id={} organization={} eventType={} actor={}",
            saved.getId(), saved.getOrganizationId(), saved.getEventType(), saved.getActor());

        return auditEntryMapper.toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEntryView> getHistory(AuditHistoryQuery query) {
        validate(query);

        log.debug("Audit action=query_history outcome=attempt organization={} eventType={}",
            query.organizationId(), query.eventType());

        return (query.projectId() != null
            ? auditEntryRepository.findAllByOrganizationIdAndProjectIdOrderByOccurredAtDesc(
                query.organizationId(), query.projectId())
            : query.eventType() == null
                ? auditEntryRepository.findAllByOrganizationIdOrderByOccurredAtDesc(query.organizationId())
                : auditEntryRepository.findAllByOrganizationIdAndEventTypeOrderByOccurredAtDesc(
                    query.organizationId(), query.eventType()))
            .stream()
            .map(auditEntryMapper::toView)
            .toList();
    }

    private static void validate(CreateAuditEntryCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
    }

    private static void validate(AuditHistoryQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query must not be null");
        }
    }
}

