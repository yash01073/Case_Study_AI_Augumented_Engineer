package com.taskbridge.notifications.service;

import com.taskbridge.notifications.domain.Notification;
import com.taskbridge.notifications.repository.NotificationRepository;
import com.taskbridge.notifications.service.command.CreateNotificationCommand;
import com.taskbridge.notifications.service.command.MarkNotificationReadCommand;
import com.taskbridge.notifications.service.query.NotificationQuery;
import com.taskbridge.notifications.service.result.NotificationView;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default application service for tenant-scoped notification workflows.
 */
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationRepository notificationRepository, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public NotificationView create(CreateNotificationCommand command) {
        validate(command);

        log.info("Notification action=create outcome=attempt organization={} recipient={} projectId={}",
            command.organizationId(), command.recipient(), command.projectId());

        Notification notification = Notification.create(
            command.organizationId(),
            command.recipient(),
            command.projectId(),
            command.message()
        );

        Notification saved = notificationRepository.save(notification);

        log.info("Notification action=create outcome=success id={} organization={} recipient={} projectId={}",
            saved.getId(), saved.getOrganizationId(), saved.getRecipient(), saved.getProjectId());

        return notificationMapper.toView(saved);
    }

    @Override
    public NotificationView markAsRead(MarkNotificationReadCommand command) {
        validate(command);

        log.info("Notification action=mark_read outcome=attempt id={} organization={}",
            command.notificationId(), command.organizationId());

        Notification notification = notificationRepository
            .findByIdAndOrganizationId(command.notificationId(), command.organizationId())
            .orElseThrow(() -> {
                log.warn("Notification action=mark_read outcome=not_found_or_org_mismatch id={} organization={}",
                    command.notificationId(), command.organizationId());
                return new EntityNotFoundException("Notification not found: " + command.notificationId());
            });

        if (!notification.getRecipient().equals(command.recipient())) {
            log.warn("Notification action=mark_read outcome=recipient_mismatch id={} organization={} expectedRecipient={} actualRecipient={}",
                command.notificationId(), command.organizationId(), notification.getRecipient(), command.recipient());
            throw new EntityNotFoundException("Notification not found: " + command.notificationId());
        }

        notification.markAsRead();
        Notification saved = notificationRepository.save(notification);

        log.info("Notification action=mark_read outcome=success id={} organization={} recipient={}",
            saved.getId(), saved.getOrganizationId(), saved.getRecipient());

        return notificationMapper.toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationView> getNotifications(NotificationQuery query) {
        validate(query);

        log.debug("Notification action=query outcome=attempt organization={} recipient={} unreadOnly={}",
            query.organizationId(), query.recipient(), query.unreadOnly());

        return (query.unreadOnly()
            ? notificationRepository.findAllByOrganizationIdAndRecipientAndReadAtIsNullOrderByCreatedAtDesc(
                query.organizationId(), query.recipient())
            : notificationRepository.findAllByOrganizationIdAndRecipientOrderByCreatedAtDesc(
                query.organizationId(), query.recipient()))
            .stream()
            .map(notificationMapper::toView)
            .toList();
    }

    private static void validate(CreateNotificationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
    }

    private static void validate(MarkNotificationReadCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
    }

    private static void validate(NotificationQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query must not be null");
        }
    }
}

