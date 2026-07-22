package com.taskbridge.common.api;

import com.taskbridge.common.exception.UnauthorizedTenantContextException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void should_returnBadRequest_when_illegalArgumentOccurs() {
        ProblemDetail problem = handler.handleIllegalArgument(
            new IllegalArgumentException("name must not be blank")
        );

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Invalid Request");
        assertThat(problem.getDetail()).isEqualTo("name must not be blank");
    }

    @Test
    void should_returnConflict_when_optimisticLockOccurs() {
        ProblemDetail problem = handler.handleOptimisticLock(
            new OptimisticLockException("stale object state")
        );

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("Concurrency Conflict");
        assertThat(problem.getDetail()).contains("modified by another request");
    }

    @Test
    void should_returnUnauthorized_when_tenantContextIsMissing() {
        ProblemDetail problem = handler.handleUnauthorizedContext(
            new UnauthorizedTenantContextException("No tenant context present — request must be authenticated")
        );

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problem.getTitle()).isEqualTo("Unauthorized");
        assertThat(problem.getDetail()).contains("No tenant context present");
    }

    @Test
    void should_returnForbidden_when_accessDeniedOccurs() {
        ProblemDetail problem = handler.handleAccessDenied(new AccessDeniedException("denied"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(problem.getTitle()).isEqualTo("Forbidden");
    }

    @Test
    void should_returnBadRequest_when_typeMismatchOccurs() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
            "abc",
            java.util.UUID.class,
            "projectId",
            null,
            new IllegalArgumentException("bad uuid")
        );

        ProblemDetail problem = handler.handleTypeMismatch(exception);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Invalid Request Parameter");
        assertThat(problem.getDetail()).contains("projectId");
    }
}

