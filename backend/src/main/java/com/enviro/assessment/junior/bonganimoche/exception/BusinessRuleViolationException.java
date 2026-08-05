package com.enviro.assessment.junior.bonganimoche.exception;

import lombok.Getter;

/**
 * Thrown when a request is structurally valid but violates a domain rule.
 *
 * Distinct from a validation error: the payload is well-formed, so the failure
 * is semantic rather than syntactic. Maps to 422 Unprocessable Entity.
 *
 * Carries an errorCode alongside the message so the frontend can branch on a
 * stable identifier instead of string-matching prose that may be reworded.
 */
@Getter
public class BusinessRuleViolationException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleViolationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}