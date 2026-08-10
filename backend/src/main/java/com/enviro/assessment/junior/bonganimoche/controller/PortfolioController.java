package com.enviro.assessment.junior.bonganimoche.controller;

import com.enviro.assessment.junior.bonganimoche.dto.response.EligibilityResponse;
import com.enviro.assessment.junior.bonganimoche.dto.response.PortfolioResponse;
import com.enviro.assessment.junior.bonganimoche.security.InvestorDetails;
import com.enviro.assessment.junior.bonganimoche.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for the authenticated investor's portfolio.
 *
 * The investor id is no longer a path variable. It comes from the authenticated
 * principal, which removes an entire class of broken-access-control bug: with
 * /investors/{id}/portfolio, any logged-in user could read another investor's
 * portfolio simply by changing the number in the URL.
 *
 * The /me prefix is a common REST convention for "the resource belonging to
 * whoever is making this request".
 */
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * GET /api/me/portfolio
     *
     * Returns investor details, every product held, each product's balance and
     * 90% withdrawal ceiling, and the total portfolio value.
     */
    @GetMapping("/portfolio")
    public ResponseEntity<PortfolioResponse> getPortfolio(
            @AuthenticationPrincipal InvestorDetails investor) {

        return ResponseEntity.ok(portfolioService.getPortfolio(investor.getInvestorId()));
    }

    /**
     * GET /api/me/products/{productId}/eligibility
     *
     * Reports whether a product may currently be withdrawn from and what the
     * maximum is, so the UI can guide the investor before submission rather
     * than only reporting failure afterwards.
     *
     * The service resolves the product scoped to the authenticated investor, so
     * requesting another investor's product id yields 404 rather than data.
     */
    @GetMapping("/products/{productId}/eligibility")
    public ResponseEntity<EligibilityResponse> getEligibility(
            @AuthenticationPrincipal InvestorDetails investor,
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                portfolioService.getEligibility(investor.getInvestorId(), productId));
    }
}