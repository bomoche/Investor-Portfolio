package com.enviro.assessment.junior.bonganimoche.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * An investor's full portfolio — personal details plus every product held.
 *
 * Deliberately excludes passwordHash. Because the API contract is defined here
 * rather than by the entity, adding a sensitive column to Investor later cannot
 * accidentally expose it through this endpoint.
 */
public record PortfolioResponse(
        Long investorId,
        String fullName,
        String email,

        /** Derived from date of birth; drives the retirement eligibility rule. */
        int age,

        /** Sum of all product balances, computed server-side. */
        BigDecimal totalPortfolioValue,

        List<ProductResponse> products
) {}