package com.taskbridge.notifications.service;

import com.taskbridge.notifications.service.command.CreateNotificationCommand;
import com.taskbridge.notifications.service.command.DispatchTeamNotificationCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that dispatches the same notification payload to each team member.
 */
@Service
@Transactional
public class TeamNotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(TeamNotificationDispatchService.class);

    private final NotificationService notificationService;

    public TeamNotificationDispatchService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Dispatches one notification per team member.
     *
     * @param command validated dispatch command
     * @return number of notifications created
     */
    public int dispatchToTeamMembers(DispatchTeamNotificationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        log.info("Notification action=dispatch_team outcome=attempt organization={} projectId={} recipients={}",
            command.organizationId(), command.projectId(), command.teamMembers().size());

        int dispatched = 0;
        for (String teamMember : command.teamMembers()) {
            notificationService.create(new CreateNotificationCommand(
                command.organizationId(),
                teamMember,
                command.projectId(),
                command.message()
            ));
            dispatched++;
        }

        log.info("Notification action=dispatch_team outcome=success organization={} projectId={} dispatched={}",
            command.organizationId(), command.projectId(), dispatched);

        return dispatched;
    }
}

