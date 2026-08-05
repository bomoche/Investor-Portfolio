package com.enviro.assessment.junior.bonganimoche.exception;

import com.enviro.assessment.junior.bonganimoche.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Centralised exception handling for the whole API.
 *
 * @RestControllerAdvice registers these handlers across every controller, which
 * keeps controllers free of try/catch and guarantees one error contract. Without
 * it, an uncaught exception reaches Spring's default error page and leaks a
 * stack trace to the client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 — the resource does not exist, or is not owned by the requester.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "Not Found",
                        "RESOURCE_NOT_FOUND", ex.getMessage(),
                        request.getRequestURI()));
    }

    /**
     * 422 — the request was well-formed but violates a domain rule.
     *
     * Deliberately not 400: the payload parsed and passed field validation, so
     * the failure is semantic. 422 tells the client the request was understood
     * and rejected on its merits, which is a meaningful distinction for the UI.
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(
            BusinessRuleViolationException ex, HttpServletRequest request) {

        log.warn("Business rule violation [{}]: {}", ex.getErrorCode(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Unprocessable Entity", ex.getErrorCode(),
                        ex.getMessage(), request.getRequestURI()));
    }

    /**
     * 400 — @Valid found constraint violations on the request body.
     *
     * Every violation is returned, not just the first, so a form can highlight
     * all offending fields in one pass instead of failing one at a time.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.FieldValidationError> fieldErrors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(error -> new ErrorResponse.FieldValidationError(
                                error.getField(), error.getDefaultMessage()))
                        .toList();

        log.warn("Validation failed on {}: {} field error(s)",
                request.getRequestURI(), fieldErrors.size());

        return ResponseEntity.badRequest().body(new ErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "VALIDATION_FAILED", "One or more fields failed validation.",
                request.getRequestURI(), fieldErrors));
    }

    /**
     * 400 — the body could not be parsed at all (malformed JSON, wrong type).
     *
     * The underlying Jackson message names internal classes, so it is logged
     * rather than returned.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed request body on {}: {}",
                request.getRequestURI(), ex.getMessage());

        return ResponseEntity.badRequest().body(
                ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                        "MALFORMED_REQUEST",
                        "Request body is missing or not valid JSON.",
                        request.getRequestURI()));
    }

    /** 400 — a path variable or query parameter was the wrong type. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("Parameter '%s' has an invalid value: '%s'.",
                ex.getName(), ex.getValue());

        return ResponseEntity.badRequest().body(
                ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                        "INVALID_PARAMETER", message, request.getRequestURI()));
    }

    /** 404 — no controller matches the requested path. */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "Not Found",
                        "ENDPOINT_NOT_FOUND",
                        "No endpoint exists for the requested path.",
                        request.getRequestURI()));
    }

    /**
     * 500 — the catch-all for anything unanticipated.
     *
     * Logged at ERROR with the full stack trace for diagnosis, but the client
     * receives only a generic message. Returning ex.getMessage() here would
     * expose internals such as SQL fragments or class names.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception on {}", request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error", "INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again later.",
                        request.getRequestURI()));
    }
}