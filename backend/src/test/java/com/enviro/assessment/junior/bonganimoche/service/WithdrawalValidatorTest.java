package com.enviro.assessment.junior.bonganimoche.service;

import com.enviro.assessment.junior.bonganimoche.entity.Investor;
import com.enviro.assessment.junior.bonganimoche.entity.Product;
import com.enviro.assessment.junior.bonganimoche.entity.enums.ProductType;
import com.enviro.assessment.junior.bonganimoche.exception.BusinessRuleViolationException;
import com.enviro.assessment.junior.bonganimoche.util.WithdrawalRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the withdrawal rule set.
 *
 * No Spring context and no database — the validator is a plain object, so these
 * run in milliseconds. That is the payoff for extracting the rules out of the
 * service rather than inlining them.
 */
class WithdrawalValidatorTest {

    private WithdrawalValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WithdrawalValidator();
    }

    /** Ages are derived from date of birth, so fixtures are built relative to today. */
    private Investor investorAged(int age) {
        return Investor.builder()
                .id(1L)
                .firstName("Test")
                .lastName("Investor")
                .email("test@enviro365.co.za")
                .passwordHash("irrelevant")
                .dateOfBirth(LocalDate.now().minusYears(age).minusDays(1))
                .build();
    }

    private Product product(ProductType type, String balance) {
        return Product.builder()
                .id(1L)
                .productName("Test Product")
                .productType(type)
                .currentBalance(new BigDecimal(balance))
                .build();
    }

    @Nested
    @DisplayName("Retirement age rule")
    class RetirementAgeRule {

        @Test
        @DisplayName("rejects a retirement withdrawal below the minimum age")
        void rejectsUnderageRetirementWithdrawal() {
            Investor investor = investorAged(40);
            Product retirement = product(ProductType.RETIREMENT, "100000.00");

            assertThatThrownBy(() ->
                    validator.validate(investor, retirement, new BigDecimal("1000.00")))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasFieldOrPropertyWithValue("errorCode",
                            WithdrawalRules.CODE_RETIREMENT_AGE);
        }

        @Test
        @DisplayName("permits a retirement withdrawal above the minimum age")
        void permitsRetirementWithdrawalOverAge() {
            Investor investor = investorAged(70);
            Product retirement = product(ProductType.RETIREMENT, "100000.00");

            assertThatCode(() ->
                    validator.validate(investor, retirement, new BigDecimal("1000.00")))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rejects exactly at the boundary age, since the rule is age > 65")
        void rejectsAtBoundaryAge() {
            // The specification states "age > 65", so 65 itself does not qualify.
            // This test pins that interpretation so it cannot drift silently.
            Investor investor = investorAged(WithdrawalRules.RETIREMENT_MINIMUM_AGE);
            Product retirement = product(ProductType.RETIREMENT, "100000.00");

            assertThatThrownBy(() ->
                    validator.validate(investor, retirement, new BigDecimal("1000.00")))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test
        @DisplayName("does not apply the age rule to savings products")
        void ignoresAgeForSavingsProducts() {
            Investor investor = investorAged(25);
            Product savings = product(ProductType.SAVINGS, "100000.00");

            assertThatCode(() ->
                    validator.validate(investor, savings, new BigDecimal("1000.00")))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Balance rules")
    class BalanceRules {

        private final Investor investor = investorAged(45);

        @Test
        @DisplayName("rejects a withdrawal exceeding the available balance")
        void rejectsWithdrawalOverBalance() {
            Product savings = product(ProductType.SAVINGS, "50000.00");

            assertThatThrownBy(() ->
                    validator.validate(investor, savings, new BigDecimal("60000.00")))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasFieldOrPropertyWithValue("errorCode",
                            WithdrawalRules.CODE_EXCEEDS_BALANCE);
        }

        @Test
        @DisplayName("rejects a withdrawal above 90% but within the balance")
        void rejectsWithdrawalOverLimit() {
            Product savings = product(ProductType.SAVINGS, "50000.00");

            // 46000 is affordable but exceeds the 45000 ceiling — this is the
            // case that distinguishes the two rules.
            assertThatThrownBy(() ->
                    validator.validate(investor, savings, new BigDecimal("46000.00")))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasFieldOrPropertyWithValue("errorCode",
                            WithdrawalRules.CODE_EXCEEDS_LIMIT);
        }

        @Test
        @DisplayName("permits a withdrawal at exactly the 90% ceiling")
        void permitsWithdrawalAtExactLimit() {
            Product savings = product(ProductType.SAVINGS, "50000.00");

            assertThatCode(() ->
                    validator.validate(investor, savings, new BigDecimal("45000.00")))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("reports the balance rule first when both are violated")
        void balanceRuleTakesPrecedence() {
            Product savings = product(ProductType.SAVINGS, "50000.00");

            assertThatThrownBy(() ->
                    validator.validate(investor, savings, new BigDecimal("99999.00")))
                    .hasFieldOrPropertyWithValue("errorCode",
                            WithdrawalRules.CODE_EXCEEDS_BALANCE);
        }
    }

    @Test
    @DisplayName("rounds the maximum withdrawal down to the cent")
    void roundsMaximumDown() {
        // 90% of 1000.55 is 900.495. Rounding up to 900.50 would advertise a
        // ceiling that the rule itself then rejects.
        BigDecimal maximum = validator.calculateMaximumWithdrawal(new BigDecimal("1000.55"));

        assertThat(maximum).isEqualByComparingTo("900.49");
        assertThat(maximum.scale()).isEqualTo(2);
    }
}