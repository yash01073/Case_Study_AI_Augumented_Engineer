package com.taskbridge.notifications.service.command;

import java.util.List;
import java.util.UUID;

/**
 * Application command for dispatching the same notification to all members of a team.
 *
 * @param organizationId owning organization identifier
 * @param projectId related project identifier
 * @param message notification message to dispatch to each recipient
 * @param teamMembers recipient identifiers representing team members
 */
public record DispatchTeamNotificationCommand(
    UUID organizationId,
    UUID projectId,
    String message,
    List<String> teamMembers
) {
    /**
     * Validates required command state.
     */
    public DispatchTeamNotificationCommand {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId must not be null");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("projectId must not be null");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        if (teamMembers == null || teamMembers.isEmpty()) {
            throw new IllegalArgumentException("teamMembers must not be empty");
        }
        if (teamMembers.stream().anyMatch(member -> member == null || member.trim().isEmpty())) {
            throw new IllegalArgumentException("teamMembers must not contain blank recipients");
        }
    }
}

