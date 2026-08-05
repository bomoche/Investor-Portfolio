package com.enviro.assessment.junior.bonganimoche.controller;

import com.enviro.assessment.junior.bonganimoche.dto.request.WithdrawalRequest;
import com.enviro.assessment.junior.bonganimoche.dto.response.WithdrawalResponse;
import com.enviro.assessment.junior.bonganimoche.service.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for withdrawal notices.
 *
 * NOTE: investorId is a path variable for now; feature/09-auth-security
 * resolves it from the authenticated principal instead.
 */
@RestController
@RequestMapping("/investors/{investorId}/withdrawals")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    /**
     * POST /api/investors/{investorId}/withdrawals
     *
     * @Valid triggers the Bean Validation constraints on WithdrawalRequest.
     * A violation throws MethodArgumentNotValidException before the service is
     * reached; feature/07 turns that into a structured 400 response.
     *
     * Returns 201 Created — a new resource has been created, so 200 would
     * understate what happened.
     */
    @PostMapping
    public ResponseEntity<WithdrawalResponse> createWithdrawal(
            @PathVariable Long investorId,
            @Valid @RequestBody WithdrawalRequest request) {

        WithdrawalResponse response = withdrawalService.createWithdrawal(investorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** GET /api/investors/{investorId}/withdrawals */
    @GetMapping
    public ResponseEntity<List<WithdrawalResponse>> getWithdrawalHistory(
            @PathVariable Long investorId) {
        return ResponseEntity.ok(withdrawalService.getWithdrawalHistory(investorId));
    }
}