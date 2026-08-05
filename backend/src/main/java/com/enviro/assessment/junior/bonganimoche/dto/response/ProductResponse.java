package com.enviro.assessment.junior.bonganimoche.dto.response;

import com.enviro.assessment.junior.bonganimoche.entity.enums.ProductType;

import java.math.BigDecimal;

/**
 * A single product as exposed by the API.
 *
 * A record rather than a class: DTOs are immutable value carriers, and records
 * give the constructor, accessors, equals and hashCode with no Lombok involved.
 */
public record ProductResponse(
        Long id,
        String productName,
        ProductType productType,
        BigDecimal currentBalance,

        /**
         * Pre-computed on the server so the UI never has to encode the 90% rule
         * itself. The withdrawal form reads this directly as its maximum.
         */
        BigDecimal maximumWithdrawal
) {}