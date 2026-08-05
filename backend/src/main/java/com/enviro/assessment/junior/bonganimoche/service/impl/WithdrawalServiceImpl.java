package com.enviro.assessment.junior.bonganimoche.service.impl;

import com.enviro.assessment.junior.bonganimoche.dto.request.WithdrawalRequest;
import com.enviro.assessment.junior.bonganimoche.dto.response.WithdrawalResponse;
import com.enviro.assessment.junior.bonganimoche.entity.Product;
import com.enviro.assessment.junior.bonganimoche.entity.WithdrawalNotice;
import com.enviro.assessment.junior.bonganimoche.entity.enums.WithdrawalStatus;
import com.enviro.assessment.junior.bonganimoche.mapper.WithdrawalMapper;
import com.enviro.assessment.junior.bonganimoche.repository.ProductRepository;
import com.enviro.assessment.junior.bonganimoche.repository.WithdrawalNoticeRepository;
import com.enviro.assessment.junior.bonganimoche.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    private final ProductRepository productRepository;
    private final WithdrawalNoticeRepository withdrawalNoticeRepository;
    private final WithdrawalMapper withdrawalMapper;

    private static final String REFERENCE_PREFIX = "WDR";
    private static final int MONETARY_SCALE = 2;

    /**
     * Creates a withdrawal notice and debits the product balance.
     *
     * The whole method runs in one transaction. Reading the balance, writing
     * the notice and updating the product must succeed or fail together — a
     * notice recorded without the matching debit would corrupt the ledger.
     */
    @Override
    @Transactional
    public WithdrawalResponse createWithdrawal(Long investorId, WithdrawalRequest request) {
        log.debug("Creating withdrawal of {} on product {} for investor {}",
                request.amount(), request.productId(), investorId);

        // Scoped by investor id, not just product id. Looking up by product id
        // alone would let an investor withdraw from someone else's product by
        // guessing an id — a broken-access-control vulnerability.
        Product product = productRepository
                .findByIdAndInvestorId(request.productId(), investorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found with id " + request.productId()
                                + " for investor " + investorId));

        // === Business rule enforcement slots in here on feature/06 ===

        BigDecimal amount = request.amount().setScale(MONETARY_SCALE, RoundingMode.HALF_UP);
        BigDecimal balanceBefore = product.getCurrentBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        // Debiting the stored balance is what makes the modelling decision work:
        // Product.currentBalance is the single source of truth, and it is only
        // ever mutated inside this transactional method.
        product.setCurrentBalance(balanceAfter);

        WithdrawalNotice notice = WithdrawalNotice.builder()
                .reference(generateReference())
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status(WithdrawalStatus.COMPLETED)
                .product(product)
                .build();

        WithdrawalNotice saved = withdrawalNoticeRepository.save(notice);
        log.info("Withdrawal {} created: {} debited from product {}",
                saved.getReference(), amount, product.getId());

        return withdrawalMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithdrawalResponse> getWithdrawalHistory(Long investorId) {
        return withdrawalNoticeRepository
                .findByProductInvestorIdOrderByRequestedAtDesc(investorId)
                .stream()
                .map(withdrawalMapper::toResponse)
                .toList();
    }

    /**
     * Builds a reference of the form WDR-2026-000042.
     *
     * Derived from the row count, which is adequate for a single-instance
     * application. Under genuine concurrency two transactions could compute the
     * same count, so the reference column carries a unique constraint and this
     * loop retries on collision. A database sequence would be the production
     * answer; this keeps the H2 setup self-contained.
     */
    private String generateReference() {
        long sequence = withdrawalNoticeRepository.count() + 1;
        String reference;
        do {
            reference = String.format("%s-%d-%06d", REFERENCE_PREFIX, Year.now().getValue(), sequence);
            sequence++;
        } while (withdrawalNoticeRepository.existsByReference(reference));
        return reference;
    }
}