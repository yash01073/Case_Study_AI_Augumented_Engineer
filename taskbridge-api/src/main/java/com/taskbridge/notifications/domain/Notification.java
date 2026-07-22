package com.taskbridge.notifications.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Tenant-scoped notification for project-related user messaging.
 */
@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notifications_org_recipient_read", columnList = "organization_id, recipient, read_at"),
        @Index(name = "idx_notifications_org_project_time", columnList = "organization_id, project_id, created_at"),
        @Index(name = "idx_notifications_org_time", columnList = "organization_id, created_at")
    }
)
public class Notification {

    private static final int RECIPIENT_MAX_LENGTH = 255;
    private static final int MESSAGE_MAX_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @NotBlank
    @Size(max = RECIPIENT_MAX_LENGTH)
    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @NotNull
    @Column(name = "project_id", nullable = false, updatable = false)
    private UUID projectId;

    @NotBlank
    @Size(max = MESSAGE_MAX_LENGTH)
    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @Column(name = "read_at")
    private Instant readAt;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Notification() {
    }

    /**
     * Creates a new unread notification.
     *
     * @param organizationId owning organization identifier
     * @param recipient recipient user identifier
     * @param projectId related project identifier
     * @param message human-readable notification message
     * @return new unread notification
     */
    public static Notification create(UUID organizationId, String recipient, UUID projectId, String message) {
        Notification notification = new Notification();
        notification.organizationId = Objects.requireNonNull(organizationId, "organizationId must not be null");
        notification.recipient = requireNonBlank(recipient, "recipient", RECIPIENT_MAX_LENGTH);
        notification.projectId = Objects.requireNonNull(projectId, "projectId must not be null");
        notification.message = requireNonBlank(message, "message", MESSAGE_MAX_LENGTH);
        return notification;
    }

    /**
     * Marks the notification as read.
     *
     * @throws IllegalStateException if the notification has already been marked as read
     */
    public void markAsRead() {
        if (readAt != null) {
            throw new IllegalStateException("Notification is already marked as read");
        }
        readAt = Instant.now();
    }

    /**
     * Indicates whether this notification has been read.
     *
     * @return {@code true} when {@code readAt} has been set
     */
    public boolean isRead() {
        return readAt != null;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    private static String requireNonBlank(String value, String fieldName, int maxLength) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }

        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass()
            : getClass();
        Class<?> otherEffectiveClass = o instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();

        if (thisEffectiveClass != otherEffectiveClass) {
            return false;
        }

        Notification other = (Notification) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getRecipient() {
        return recipient;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getMessage() {
        return message;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

