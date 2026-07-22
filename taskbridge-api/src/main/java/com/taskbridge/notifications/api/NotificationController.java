package com.taskbridge.notifications.api;

import com.taskbridge.notifications.dto.NotificationResponse;
import com.taskbridge.notifications.service.NotificationService;
import com.taskbridge.notifications.service.command.MarkNotificationReadCommand;
import com.taskbridge.notifications.service.query.NotificationQuery;
import com.taskbridge.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for recipient-scoped notification reads and read-state updates.
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationApiMapper notificationApiMapper;

    public NotificationController(NotificationService notificationService, NotificationApiMapper notificationApiMapper) {
        this.notificationService = notificationService;
        this.notificationApiMapper = notificationApiMapper;
    }

    /**
     * Returns notifications for the requested user when authorized.
     *
     * @param userId target user identifier
     * @param unreadOnly optional unread-only filter
     * @return notification list ordered by creation time descending
     */
    @GetMapping("/{userId}")
    @PreAuthorize("@authorizationService.canReadNotifications(authentication, #userId)")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
        @PathVariable String userId,
        @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        UUID organizationId = TenantContext.requireTenantId();

        List<NotificationResponse> response = notificationService
            .getNotifications(new NotificationQuery(organizationId, userId, unreadOnly))
            .stream()
            .map(notificationApiMapper::toResponse)
            .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Marks a notification as read for the authenticated user.
     *
     * @param id notification identifier
     * @return updated notification response
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("@authorizationService.canMarkNotificationRead(authentication)")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable UUID id) {
        UUID organizationId = TenantContext.requireTenantId();
        String recipient = TenantContext.requireUserId();

        NotificationResponse response = notificationApiMapper.toResponse(
            notificationService.markAsRead(new MarkNotificationReadCommand(organizationId, id, recipient))
        );

        return ResponseEntity.ok(response);
    }
}

