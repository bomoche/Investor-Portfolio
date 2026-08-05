package com.enviro.assessment.junior.bonganimoche.dto.response;

import java.math.BigDecimal;

/**
 * The withdrawal constraints applying to one product, so the UI can guide the
 * investor before submission rather than only reporting failure afterwards.
 *
 * The server remains the authority — this endpoint informs the interface, it
 * does not replace validation on the write path.
 */
public record EligibilityResponse(
        Long productId,
        String productName,
        boolean eligible,
        BigDecimal currentBalance,
        BigDecimal maximumWithdrawal,
        String reason
) {}