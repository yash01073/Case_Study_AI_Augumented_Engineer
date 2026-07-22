package com.taskbridge.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Helper bean for Spring method-security expressions.
 * <p>
 * The current implementation acts as a lightweight authorization hook that can
 * be tightened later to enforce role, permission, and team membership rules.
 * </p>
 */
@Component("authorizationService")
public class AuthorizationService {

    /**
     * Indicates whether the authenticated actor may create audit entries.
     *
     * @param authentication current authentication
     * @return {@code true} when the request is authenticated
     */
    public boolean canWriteAudit(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Indicates whether the authenticated actor may read audit history.
     *
     * @param authentication current authentication
     * @return {@code true} when the request is authenticated
     */
    public boolean canReadAudit(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Indicates whether the authenticated actor may read notifications for the requested user.
     *
     * @param authentication current authentication
     * @param userId path user identifier
     * @return {@code true} when the authenticated principal matches the requested user
     */
    public boolean canReadNotifications(Authentication authentication, String userId) {
        return authentication != null
            && authentication.isAuthenticated()
            && authentication.getName() != null
            && authentication.getName().equals(userId);
    }

    /**
     * Indicates whether the authenticated actor may mark their notification as read.
     *
     * @param authentication current authentication
     * @return {@code true} when the request is authenticated
     */
    public boolean canMarkNotificationRead(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }
}

