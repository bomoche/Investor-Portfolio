package com.enviro.assessment.junior.bonganimoche.dto.response;

/**
 * Issued on successful login. Returns the investor's display details alongside
 * the token so the UI can render a header without an extra request.
 */
public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        Long investorId,
        String fullName,
        String email
) {}