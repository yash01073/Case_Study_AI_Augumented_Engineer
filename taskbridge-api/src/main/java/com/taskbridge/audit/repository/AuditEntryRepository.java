package com.taskbridge.audit.repository;

import com.taskbridge.audit.domain.AuditEntry;
import com.taskbridge.audit.domain.AuditEventType;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Tenant-scoped repository operations for immutable audit entries.
 */
@Repository
public interface AuditEntryRepository extends Repository<AuditEntry, UUID> {

    /**
     * Persists a new audit entry.
     *
     * @param entity audit entry to persist
     * @param <S> entity type
     * @return persisted entity
     */
    <S extends AuditEntry> S save(S entity);

    /**
     * Loads an audit entry by identifier and organization.
     *
     * @param id audit entry identifier
     * @param organizationId owning organization identifier
     * @return optional matching entry
     */
    Optional<AuditEntry> findByIdAndOrganizationId(UUID id, UUID organizationId);

    /**
     * Loads audit entries for an organization ordered by occurrence descending.
     *
     * @param organizationId owning organization identifier
     * @return matching immutable entries
     */
    List<AuditEntry> findAllByOrganizationIdOrderByOccurredAtDesc(UUID organizationId);

    /**
     * Loads audit entries for a project inside an organization ordered by occurrence descending.
     *
     * @param organizationId owning organization identifier
     * @param projectId related project identifier
     * @return matching immutable entries
     */
    List<AuditEntry> findAllByOrganizationIdAndProjectIdOrderByOccurredAtDesc(UUID organizationId, UUID projectId);

    /**
     * Loads audit entries for an organization and event type ordered by occurrence descending.
     *
     * @param organizationId owning organization identifier
     * @param eventType event type filter
     * @return matching immutable entries
     */
    List<AuditEntry> findAllByOrganizationIdAndEventTypeOrderByOccurredAtDesc(UUID organizationId, AuditEventType eventType);
}

