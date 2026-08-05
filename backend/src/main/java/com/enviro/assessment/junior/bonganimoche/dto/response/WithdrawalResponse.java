package com.enviro.assessment.junior.bonganimoche.dto.response;

import com.enviro.assessment.junior.bonganimoche.entity.enums.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A withdrawal notice as exposed by the API.
 *
 * Flattens the product name onto the response so the history table can render
 * without a second request, while omitting the nested product entity that
 * would otherwise drag the whole object graph into the JSON.
 */
public record WithdrawalResponse(
        Long id,
        String reference,
        Long productId,
        String productName,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        WithdrawalStatus status,
        LocalDateTime requestedAt
) {}