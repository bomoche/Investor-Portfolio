package com.enviro.assessment.junior.bonganimoche.mapper;

import com.enviro.assessment.junior.bonganimoche.dto.response.WithdrawalResponse;
import com.enviro.assessment.junior.bonganimoche.entity.WithdrawalNotice;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalMapper {

    public WithdrawalResponse toResponse(WithdrawalNotice notice) {
        return new WithdrawalResponse(
                notice.getId(),
                notice.getReference(),
                notice.getProduct().getId(),
                notice.getProduct().getProductName(),
                notice.getAmount(),
                notice.getBalanceBefore(),
                notice.getBalanceAfter(),
                notice.getStatus(),
                notice.getRequestedAt()
        );
    }
}