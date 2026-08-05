package com.enviro.assessment.junior.bonganimoche.service.impl;

import com.enviro.assessment.junior.bonganimoche.entity.Investor;
import com.enviro.assessment.junior.bonganimoche.entity.WithdrawalNotice;
import com.enviro.assessment.junior.bonganimoche.entity.enums.WithdrawalStatus;
import com.enviro.assessment.junior.bonganimoche.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.bonganimoche.repository.InvestorRepository;
import com.enviro.assessment.junior.bonganimoche.repository.WithdrawalNoticeRepository;
import com.enviro.assessment.junior.bonganimoche.service.StatementService;
import com.enviro.assessment.junior.bonganimoche.util.CsvWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final WithdrawalNoticeRepository withdrawalNoticeRepository;
    private final InvestorRepository investorRepository;

    /** ISO-8601 — sorts lexicographically and parses unambiguously anywhere. */
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<String> HEADERS = List.of(
            "Reference", "Date", "Product", "Product Type",
            "Amount (ZAR)", "Balance Before", "Balance After", "Status");

    @Override
    @Transactional(readOnly = true)
    public String generateWithdrawalStatement(Long investorId, Long productId,
                                              WithdrawalStatus status,
                                              LocalDate from, LocalDate to) {

        // Confirms the investor exists so an unknown id returns 404 rather than
        // an empty file, which would be indistinguishable from "no withdrawals".
        Investor investor = investorRepository.findById(investorId)
                .orElseThrow(() -> ResourceNotFoundException.forInvestor(investorId));

        // Dates arrive as calendar days but the column is a timestamp. Widening
        // to start-of-day and end-of-day makes the range inclusive on both ends;
        // without this, a withdrawal at 14:00 on the "to" date would be excluded.
        LocalDateTime fromDateTime = (from == null) ? null : from.atStartOfDay();
        LocalDateTime toDateTime = (to == null) ? null : to.atTime(LocalTime.MAX);

        List<WithdrawalNotice> notices = withdrawalNoticeRepository.findFiltered(
                investorId, productId, status, fromDateTime, toDateTime);

        log.debug("Generating statement for investor {}: {} record(s)",
                investorId, notices.size());

        StringBuilder csv = new StringBuilder();
        csv.append(CsvWriter.toRow(HEADERS));

        for (WithdrawalNotice notice : notices) {
            csv.append(CsvWriter.toRow(List.of(
                    notice.getReference(),
                    notice.getRequestedAt().format(TIMESTAMP_FORMAT),
                    notice.getProduct().getProductName(),
                    notice.getProduct().getProductType().name(),
                    notice.getAmount().toPlainString(),
                    notice.getBalanceBefore().toPlainString(),
                    notice.getBalanceAfter().toPlainString(),
                    notice.getStatus().name()
            )));
        }

        log.info("Statement generated for investor {} ({})",
                investorId, investor.getEmail());
        return csv.toString();
    }
}