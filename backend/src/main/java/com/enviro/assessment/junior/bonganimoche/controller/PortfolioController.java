package com.enviro.assessment.junior.bonganimoche.controller;

import com.enviro.assessment.junior.bonganimoche.dto.response.PortfolioResponse;
import com.enviro.assessment.junior.bonganimoche.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for investor portfolios.
 *
 * The controller stays thin: it maps HTTP to a service call and back. All
 * business logic lives in the service layer, which keeps this testable with
 * MockMvc and a mocked service.
 *
 * NOTE: investorId is currently a path variable. Once authentication lands on
 * feature/09-auth-security it will be resolved from the security context
 * instead, so one investor cannot request another's portfolio.
 */
@RestController
@RequestMapping("/investors")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * GET /api/investors/{investorId}/portfolio
     *
     * The nested path expresses that a portfolio is a sub-resource of an
     * investor rather than a standalone entity, following REST conventions.
     */
    @GetMapping("/{investorId}/portfolio")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable Long investorId) {
        return ResponseEntity.ok(portfolioService.getPortfolio(investorId));
    }
}