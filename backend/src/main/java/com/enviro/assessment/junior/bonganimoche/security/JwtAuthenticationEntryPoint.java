package com.enviro.assessment.junior.bonganimoche.security;

import com.enviro.assessment.junior.bonganimoche.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns a structured 401 when an unauthenticated request reaches a protected
 * endpoint.
 *
 * Spring Security rejects such requests inside the filter chain, before any
 * controller method runs, so @RestControllerAdvice never sees them and the
 * default response is an empty 403 with no body. That is both the wrong status
 * and the wrong shape.
 *
 * The distinction matters to the frontend: 401 means "identify yourself" and
 * triggers a redirect to login, while 403 means "identified, still not allowed"
 * and should show an error instead. Conflating them would log a user out for
 * the wrong reason.
 *
 * A dedicated ObjectMapper is constructed here rather than injected because
 * this component runs in the servlet filter chain, outside the MVC message
 * conversion that would normally handle serialisation. JavaTimeModule is
 * registered so the LocalDateTime timestamp serialises correctly.
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.debug("Unauthenticated request to {}: {}",
                request.getRequestURI(), authException.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Deliberately generic. Distinguishing "no token" from "expired token"
        // from "bad signature" would tell an attacker which part of the token
        // to work on.
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "AUTHENTICATION_REQUIRED",
                "A valid authentication token is required to access this resource.",
                request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}