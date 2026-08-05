package com.enviro.assessment.junior.bonganimoche.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The single error shape returned by every failing endpoint.
 *
 * One consistent contract means the frontend needs one error handler rather
 * than a branch per endpoint. errorCode is the stable identifier to branch on;
 * message is prose intended for display and may be reworded freely.
 *
 * NON_NULL suppresses fieldErrors on non-validation failures, so responses stay
 * free of null noise.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String errorCode,
        String message,
        String path,
        List<FieldValidationError> fieldErrors
) {

    /** Per-field detail, so a form can highlight the offending input. */
    public record FieldValidationError(String field, String message) {}

    public static ErrorResponse of(int status, String error, String errorCode,
                                   String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, errorCode,
                message, path, null);
    }
}