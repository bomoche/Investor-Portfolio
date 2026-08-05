package com.enviro.assessment.junior.bonganimoche.controller;

import com.enviro.assessment.junior.bonganimoche.dto.request.WithdrawalRequest;
import com.enviro.assessment.junior.bonganimoche.dto.response.WithdrawalResponse;
import com.enviro.assessment.junior.bonganimoche.entity.enums.WithdrawalStatus;
import com.enviro.assessment.junior.bonganimoche.service.StatementService;
import com.enviro.assessment.junior.bonganimoche.service.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final StatementService statementService;

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

    /**
     * GET /api/investors/{investorId}/withdrawals/export
     *
     * All query parameters are optional, so the same endpoint serves both a
     * full statement and any filtered subset.
     *
     * Returns the CSV as a byte array with an explicit charset. Content-Disposition
     * attachment tells the browser to download rather than render, and supplies
     * the default filename.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStatement(
            @PathVariable Long investorId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) WithdrawalStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        String csv = statementService.generateWithdrawalStatement(
                investorId, productId, status, from, to);

        String filename = String.format("withdrawal-statement-%s.csv",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        // UTF-8 BOM so Excel on Windows detects the encoding correctly.
        // Without it, non-ASCII characters in product names render as mojibake.
        byte[] body = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                // Lets the browser read the header from a cross-origin response,
                // which the React download handler needs.
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                        HttpHeaders.CONTENT_DISPOSITION)
                .body(body);
    }
}