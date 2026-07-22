package com.taskbridge.notifications.repository;

import com.taskbridge.notifications.domain.Notification;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Tenant-scoped repository operations for notifications.
 */
@Repository
public interface NotificationRepository extends Repository<Notification, UUID> {

    /**
     * Persists a notification.
     *
     * @param entity notification entity
     * @param <S> entity type
     * @return persisted entity
     */
    <S extends Notification> S save(S entity);

    /**
     * Loads a notification by identifier and organization.
     *
     * @param id notification identifier
     * @param organizationId owning organization identifier
     * @return optional matching notification
     */
    Optional<Notification> findByIdAndOrganizationId(UUID id, UUID organizationId);

    /**
     * Loads notifications for a recipient inside an organization ordered by creation descending.
     *
     * @param organizationId owning organization identifier
     * @param recipient recipient identifier
     * @return matching notifications
     */
    List<Notification> findAllByOrganizationIdAndRecipientOrderByCreatedAtDesc(UUID organizationId, String recipient);

    /**
     * Loads unread notifications for a recipient inside an organization ordered by creation descending.
     *
     * @param organizationId owning organization identifier
     * @param recipient recipient identifier
     * @return unread notifications
     */
    List<Notification> findAllByOrganizationIdAndRecipientAndReadAtIsNullOrderByCreatedAtDesc(UUID organizationId, String recipient);
}

