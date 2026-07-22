package com.taskbridge.common.api;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

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
}

