package com.taskbridge.audit.api;

import com.taskbridge.audit.dto.AuditEntryResponse;
import com.taskbridge.audit.dto.CreateAuditEntryRequest;
import com.taskbridge.audit.service.AuditService;
import com.taskbridge.audit.service.query.AuditHistoryQuery;
import com.taskbridge.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for immutable audit entry creation and project-scoped audit history.
 */
@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditService auditService;
    private final AuditApiMapper auditApiMapper;

    public AuditController(AuditService auditService, AuditApiMapper auditApiMapper) {
        this.auditService = auditService;
        this.auditApiMapper = auditApiMapper;
    }

    /**
     * Creates a new immutable audit entry.
     *
     * @param request validated audit create request
     * @return created audit entry response
     */
    @PostMapping
    @PreAuthorize("@authorizationService.canWriteAudit(authentication)")
    public ResponseEntity<AuditEntryResponse> create(@Valid @RequestBody CreateAuditEntryRequest request) {
        UUID organizationId = TenantContext.requireTenantId();
        String actor = TenantContext.requireUserId();

        AuditEntryResponse response = auditApiMapper.toResponse(
            auditService.create(auditApiMapper.toCreateCommand(organizationId, actor, request))
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns audit history for a project inside the authenticated organization.
     *
     * @param projectId project identifier
     * @return immutable audit history ordered by occurrence descending
     */
    @GetMapping("/{projectId}")
    @PreAuthorize("@authorizationService.canReadAudit(authentication)")
    public ResponseEntity<List<AuditEntryResponse>> getByProject(@PathVariable UUID projectId) {
        UUID organizationId = TenantContext.requireTenantId();

        List<AuditEntryResponse> response = auditService
            .getHistory(new AuditHistoryQuery(organizationId, projectId, null, null, null))
            .stream()
            .map(auditApiMapper::toResponse)
            .toList();

        return ResponseEntity.ok(response);
    }
}

