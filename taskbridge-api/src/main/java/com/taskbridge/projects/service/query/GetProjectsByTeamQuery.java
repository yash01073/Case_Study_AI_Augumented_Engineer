package com.taskbridge.projects.service.query;

import java.util.UUID;

/**
 * Application query for retrieving projects by team within a tenant.
 *
 * @param tenantId owning tenant identifier
 * @param teamId   target team identifier
 */
public record GetProjectsByTeamQuery(UUID tenantId, UUID teamId) {
    /**
     * Validates required query state.
     */
    public GetProjectsByTeamQuery {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        if (teamId == null) {
            throw new IllegalArgumentException("teamId must not be null");
        }
    }
}

