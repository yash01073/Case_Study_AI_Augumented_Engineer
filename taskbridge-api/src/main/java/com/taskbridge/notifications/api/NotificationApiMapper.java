package com.taskbridge.notifications.api;

import com.taskbridge.notifications.dto.NotificationResponse;
import com.taskbridge.notifications.service.result.NotificationView;
import org.springframework.stereotype.Component;

/**
 * Maps notification application views to REST response payloads.
 */
@Component
public class NotificationApiMapper {

    /**
     * Converts an application view into an HTTP response DTO.
     *
     * @param view application view
     * @return response payload
     */
    public NotificationResponse toResponse(NotificationView view) {
        return new NotificationResponse(
            view.id(),
            view.projectId(),
            view.message(),
            view.isRead(),
            view.readAt(),
            view.createdAt()
        );
    }
}

