package com.enviro.assessment.junior.bonganimoche.service;

import com.enviro.assessment.junior.bonganimoche.entity.enums.WithdrawalStatus;

import java.time.LocalDate;

public interface StatementService {

    /**
     * Builds a CSV withdrawal statement. All filters are optional; a null value
     * means the filter is not applied.
     */
    String generateWithdrawalStatement(Long investorId, Long productId,
                                       WithdrawalStatus status,
                                       LocalDate from, LocalDate to);
}