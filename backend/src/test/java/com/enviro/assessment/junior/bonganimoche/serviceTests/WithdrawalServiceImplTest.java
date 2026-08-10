package com.enviro.assessment.junior.bonganimoche.service.impl;

import com.enviro.assessment.junior.bonganimoche.dto.request.WithdrawalRequest;
import com.enviro.assessment.junior.bonganimoche.entity.Investor;
import com.enviro.assessment.junior.bonganimoche.entity.Product;
import com.enviro.assessment.junior.bonganimoche.entity.WithdrawalNotice;
import com.enviro.assessment.junior.bonganimoche.entity.enums.ProductType;
import com.enviro.assessment.junior.bonganimoche.entity.enums.WithdrawalStatus;
import com.enviro.assessment.junior.bonganimoche.exception.BusinessRuleViolationException;
import com.enviro.assessment.junior.bonganimoche.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.bonganimoche.mapper.WithdrawalMapper;
import com.enviro.assessment.junior.bonganimoche.repository.ProductRepository;
import com.enviro.assessment.junior.bonganimoche.repository.WithdrawalNoticeRepository;
import com.enviro.assessment.junior.bonganimoche.service.WithdrawalValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the withdrawal service in isolation.
 *
 * Repositories and the validator are mocked, so these verify the service's own
 * orchestration — does it debit the balance, does it snapshot correctly, does it
 * abort before mutating when a rule fails — without a database.
 */
@ExtendWith(MockitoExtension.class)
class WithdrawalServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private WithdrawalNoticeRepository withdrawalNoticeRepository;
    @Mock private WithdrawalMapper withdrawalMapper;
    @Mock private WithdrawalValidator withdrawalValidator;

    @InjectMocks private WithdrawalServiceImpl withdrawalService;

    private Product product;

    @BeforeEach
    void setUp() {
        Investor investor = Investor.builder()
                .id(1L)
                .firstName("Thabo")
                .lastName("Mokoena")
                .email("thabo@enviro365.co.za")
                .passwordHash("irrelevant")
                .dateOfBirth(LocalDate.of(1955, 3, 14))
                .build();

        product = Product.builder()
                .id(1L)
                .productName("Enviro365 Green Savings")
                .productType(ProductType.SAVINGS)
                .currentBalance(new BigDecimal("125000.00"))
                .investor(investor)
                .build();
    }

    @Test
    @DisplayName("debits the product balance by the withdrawal amount")
    void debitsBalance() {
        when(productRepository.findByIdAndInvestorId(1L, 1L))
                .thenReturn(Optional.of(product));
        when(withdrawalNoticeRepository.count()).thenReturn(0L);
        when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        withdrawalService.createWithdrawal(1L,
                new WithdrawalRequest(1L, new BigDecimal("5000.00")));

        assertThat(product.getCurrentBalance()).isEqualByComparingTo("120000.00");
    }

    @Test
    @DisplayName("snapshots the balance before and after on the notice")
    void snapshotsBalances() {
        when(productRepository.findByIdAndInvestorId(1L, 1L))
                .thenReturn(Optional.of(product));
        when(withdrawalNoticeRepository.count()).thenReturn(0L);
        when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        withdrawalService.createWithdrawal(1L,
                new WithdrawalRequest(1L, new BigDecimal("5000.00")));

        // Captures the entity handed to save() so we can assert on what was
        // actually persisted rather than on the return value.
        ArgumentCaptor<WithdrawalNotice> captor =
                ArgumentCaptor.forClass(WithdrawalNotice.class);
        verify(withdrawalNoticeRepository).save(captor.capture());

        WithdrawalNotice saved = captor.getValue();
        assertThat(saved.getBalanceBefore()).isEqualByComparingTo("125000.00");
        assertThat(saved.getBalanceAfter()).isEqualByComparingTo("120000.00");
        assertThat(saved.getAmount()).isEqualByComparingTo("5000.00");
        assertThat(saved.getStatus()).isEqualTo(WithdrawalStatus.COMPLETED);
        assertThat(saved.getReference()).startsWith("WDR-");
    }

    @Test
    @DisplayName("leaves the balance untouched when a business rule is violated")
    void doesNotMutateOnRuleViolation() {
        when(productRepository.findByIdAndInvestorId(1L, 1L))
                .thenReturn(Optional.of(product));
        doThrow(new BusinessRuleViolationException("EXCEEDS_LIMIT", "too much"))
                .when(withdrawalValidator).validate(any(), any(), any());

        assertThatThrownBy(() -> withdrawalService.createWithdrawal(1L,
                new WithdrawalRequest(1L, new BigDecimal("999999.00"))))
                .isInstanceOf(BusinessRuleViolationException.class);

        // The ordering guarantee that matters: validation runs before mutation.
        assertThat(product.getCurrentBalance()).isEqualByComparingTo("125000.00");
        verify(withdrawalNoticeRepository, never()).save(any());
    }

    @Test
    @DisplayName("rejects a product belonging to another investor")
    void rejectsProductOwnedByAnotherInvestor() {
        // The repository is scoped by investor, so another investor's product
        // simply is not found — which is what produces the 404.
        when(productRepository.findByIdAndInvestorId(1L, 2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> withdrawalService.createWithdrawal(2L,
                new WithdrawalRequest(1L, new BigDecimal("100.00"))))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(withdrawalValidator, never()).validate(any(), any(), any());
    }
}