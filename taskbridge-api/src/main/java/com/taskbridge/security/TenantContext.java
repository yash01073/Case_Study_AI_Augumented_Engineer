package com.taskbridge.security;

import com.taskbridge.common.exception.UnauthorizedTenantContextException;

import java.util.UUID;

/**
 * Thread-local holder for the current tenant identity.
 * Populated by {@link JwtAuthFilter} from trusted JWT claims.
 * Always clear the context after request processing.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> USER  = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UUID tenantId, String userId) {
        TENANT.set(tenantId);
        USER.set(userId);
    }

    public static UUID requireTenantId() {
        UUID id = TENANT.get();
        if (id == null) {
            throw new UnauthorizedTenantContextException("No tenant context present — request must be authenticated");
        }
        return id;
    }

    public static String requireUserId() {
        String id = USER.get();
        if (id == null) {
            throw new UnauthorizedTenantContextException("No user context present — request must be authenticated");
        }
        return id;
    }

    public static void clear() {
        TENANT.remove();
        USER.remove();
    }
}

