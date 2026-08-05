package com.enviro.assessment.junior.bonganimoche.service;

import com.enviro.assessment.junior.bonganimoche.dto.request.WithdrawalRequest;
import com.enviro.assessment.junior.bonganimoche.dto.response.WithdrawalResponse;

import java.util.List;

public interface WithdrawalService {

    WithdrawalResponse createWithdrawal(Long investorId, WithdrawalRequest request);

    List<WithdrawalResponse> getWithdrawalHistory(Long investorId);
}