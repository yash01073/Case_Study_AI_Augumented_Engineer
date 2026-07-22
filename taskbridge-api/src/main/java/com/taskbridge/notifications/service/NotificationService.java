package com.taskbridge.notifications.service;

import com.taskbridge.notifications.service.command.CreateNotificationCommand;
import com.taskbridge.notifications.service.command.MarkNotificationReadCommand;
import com.taskbridge.notifications.service.query.NotificationQuery;
import com.taskbridge.notifications.service.result.NotificationView;

import java.util.List;

/**
 * Application service for tenant-scoped notification creation, querying, and read tracking.
 */
public interface NotificationService {

    /**
     * Creates a new unread notification.
     *
     * @param command validated notification create command
     * @return persisted notification as an application view
     */
    NotificationView create(CreateNotificationCommand command);

    /**
     * Marks a tenant-scoped notification as read.
     *
     * @param command validated read command
     * @return updated notification as an application view
     */
    NotificationView markAsRead(MarkNotificationReadCommand command);

    /**
     * Retrieves recipient-scoped notifications.
     *
     * @param query validated notification query
     * @return notifications ordered by creation time descending
     */
    List<NotificationView> getNotifications(NotificationQuery query);
}

