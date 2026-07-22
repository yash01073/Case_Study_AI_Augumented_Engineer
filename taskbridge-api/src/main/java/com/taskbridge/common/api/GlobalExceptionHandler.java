package com.taskbridge.common.api;

import com.taskbridge.common.exception.UnauthorizedTenantContextException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translates exceptions to RFC 7807 ProblemDetail responses.
 * Logs security events at WARN and unexpected errors at ERROR.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                fe -> fe.getField(),
                fe -> fe.getDefaultMessage(),
                (a, b) -> a
            ));

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("https://taskbridge.io/errors/validation"));
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("https://taskbridge.io/errors/not-found"));
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(UnauthorizedTenantContextException.class)
    public ProblemDetail handleUnauthorizedContext(UnauthorizedTenantContextException ex) {
        log.warn("Unauthorized request context: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized");
        problem.setType(URI.create("https://taskbridge.io/errors/unauthorized"));
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Forbidden");
        problem.setType(URI.create("https://taskbridge.io/errors/forbidden"));
        problem.setDetail("You do not have permission to perform this action.");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid Request");
        problem.setType(URI.create("https://taskbridge.io/errors/invalid-request"));
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid Request Parameter");
        problem.setType(URI.create("https://taskbridge.io/errors/invalid-parameter"));
        problem.setDetail("Invalid value for parameter '%s'.".formatted(ex.getName()));
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Operation Not Allowed");
        problem.setType(URI.create("https://taskbridge.io/errors/conflict"));
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ProblemDetail handleOptimisticLock(Exception ex) {
        log.warn("Concurrent update conflict: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Concurrency Conflict");
        problem.setType(URI.create("https://taskbridge.io/errors/concurrency-conflict"));
        problem.setDetail("The resource was modified by another request. Please retry.");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://taskbridge.io/errors/internal"));
        return problem;
    }
}

