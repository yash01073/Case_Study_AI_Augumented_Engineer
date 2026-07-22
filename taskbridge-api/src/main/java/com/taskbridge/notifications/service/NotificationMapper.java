package com.taskbridge.notifications.service;

import com.taskbridge.notifications.domain.Notification;
import com.taskbridge.notifications.service.result.NotificationView;
import org.springframework.stereotype.Component;

/**
 * Maps notification entities to transport-neutral application views.
 */
@Component
public class NotificationMapper {

    /**
     * Converts a notification entity into an application view.
     *
     * @param notification notification entity
     * @return immutable application view
     */
    public NotificationView toView(Notification notification) {
        return new NotificationView(
            notification.getId(),
            notification.getOrganizationId(),
            notification.getRecipient(),
            notification.getProjectId(),
            notification.getMessage(),
            notification.getReadAt(),
            notification.getCreatedAt()
        );
    }
}

