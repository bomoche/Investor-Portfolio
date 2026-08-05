package com.enviro.assessment.junior.bonganimoche.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Payload for creating a withdrawal notice.
 *
 * Constraints here are structural — is the input well-formed? They are checked
 * by @Valid in the controller before any service code runs, so the service can
 * assume a non-null, positive, correctly-scaled amount.
 *
 * Business rules (age, balance, 90% ceiling) are NOT expressed here. They
 * depend on database state and belong in the service layer.
 */
public record WithdrawalRequest(

        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Withdrawal amount is required")
        @DecimalMin(value = "0.01", message = "Withdrawal amount must be greater than zero")
        @Digits(integer = 17, fraction = 2,
                message = "Withdrawal amount may have at most 2 decimal places")
        BigDecimal amount
) {}