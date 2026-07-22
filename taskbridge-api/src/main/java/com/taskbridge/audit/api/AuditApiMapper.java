package com.taskbridge.audit.api;

import com.taskbridge.audit.dto.AuditEntryResponse;
import com.taskbridge.audit.dto.CreateAuditEntryRequest;
import com.taskbridge.audit.service.command.CreateAuditEntryCommand;
import com.taskbridge.audit.service.result.AuditEntryView;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Maps audit HTTP DTOs to application commands and application views to HTTP responses.
 */
@Component
public class AuditApiMapper {

    /**
     * Converts an HTTP request into an application command.
     *
     * @param organizationId authenticated organization identifier
     * @param actor authenticated actor identifier
     * @param request validated HTTP payload
     * @return application command
     */
    public CreateAuditEntryCommand toCreateCommand(
        UUID organizationId,
        String actor,
        CreateAuditEntryRequest request
    ) {
        return new CreateAuditEntryCommand(
            organizationId,
            request.projectId(),
            request.eventType(),
            request.previousState(),
            request.newState(),
            actor
        );
    }

    /**
     * Converts an application view into a REST response DTO.
     *
     * @param view application view
     * @return response payload
     */
    public AuditEntryResponse toResponse(AuditEntryView view) {
        return new AuditEntryResponse(
            view.id(),
            view.projectId(),
            view.eventType(),
            view.previousState(),
            view.newState(),
            view.actor(),
            view.occurredAt()
        );
    }
}

