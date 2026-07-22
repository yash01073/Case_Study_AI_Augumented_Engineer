package com.taskbridge.common.exception;

/**
 * Thrown when a request reaches the application layer without an authenticated
 * tenant and user context.
 */
public class UnauthorizedTenantContextException extends RuntimeException {

    /**
     * Creates a new unauthorized context exception.
     *
     * @param message exception detail message
     */
    public UnauthorizedTenantContextException(String message) {
        super(message);
    }
}

