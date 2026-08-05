package com.enviro.assessment.junior.bonganimoche.service;

import com.enviro.assessment.junior.bonganimoche.entity.Investor;
import com.enviro.assessment.junior.bonganimoche.entity.Product;
import com.enviro.assessment.junior.bonganimoche.entity.enums.ProductType;
import com.enviro.assessment.junior.bonganimoche.exception.BusinessRuleViolationException;
import com.enviro.assessment.junior.bonganimoche.util.WithdrawalRules;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Enforces the domain rules governing a withdrawal.
 *
 * Extracted from the service rather than inlined for two reasons: the rules are
 * the part of this system most likely to change, and isolating them means they
 * can be unit tested with plain objects and no Spring context at all.
 *
 * Every method throws rather than returning a boolean — a rule violation is an
 * exceptional outcome that must abort the transaction, and a returned false is
 * far easier to forget to check.
 */
@Slf4j
@Component
public class WithdrawalValidator {

    /**
     * Runs the full rule set against a proposed withdrawal.
     *
     * Order is deliberate. The age rule comes first because it disqualifies the
     * product outright — telling someone under 65 that their retirement
     * withdrawal is 500 rand over the limit would be misleading advice. The
     * balance rule precedes the 90% rule so that asking for more than you have
     * gets the clearer of the two messages.
     */
    public void validate(Investor investor, Product product, BigDecimal amount) {
        validateRetirementAge(investor, product);
        validateAgainstBalance(product, amount);
        validateAgainstWithdrawalLimit(product, amount);
    }

    /** Rule 1: retirement withdrawals are only permitted above age 65. */
    private void validateRetirementAge(Investor investor, Product product) {
        if (product.getProductType() != ProductType.RETIREMENT) {
            return;
        }
        int age = investor.getAge();
        if (age <= WithdrawalRules.RETIREMENT_MINIMUM_AGE) {
            log.warn("Retirement withdrawal blocked for investor {} aged {}",
                    investor.getId(), age);
            throw new BusinessRuleViolationException(
                    WithdrawalRules.CODE_RETIREMENT_AGE,
                    String.format(
                            "Retirement withdrawals are only permitted from age %d. Current age: %d.",
                            WithdrawalRules.RETIREMENT_MINIMUM_AGE + 1, age));
        }
    }

    /** Rule 2: a withdrawal may not exceed the available balance. */
    private void validateAgainstBalance(Product product, BigDecimal amount) {
        // compareTo, not equals: BigDecimal.equals compares scale as well, so
        // 100.0 and 100.00 would be unequal. Only the numeric value matters.
        if (amount.compareTo(product.getCurrentBalance()) > 0) {
            throw new BusinessRuleViolationException(
                    WithdrawalRules.CODE_EXCEEDS_BALANCE,
                    String.format("Withdrawal of R%s exceeds the available balance of R%s.",
                            amount.setScale(2, RoundingMode.HALF_UP),
                            product.getCurrentBalance()));
        }
    }

    /** Rule 3: a withdrawal may not exceed 90% of the balance. */
    private void validateAgainstWithdrawalLimit(Product product, BigDecimal amount) {
        BigDecimal maximum = calculateMaximumWithdrawal(product.getCurrentBalance());
        if (amount.compareTo(maximum) > 0) {
            throw new BusinessRuleViolationException(
                    WithdrawalRules.CODE_EXCEEDS_LIMIT,
                    String.format(
                            "Withdrawal of R%s exceeds the maximum of R%s (90%% of the balance).",
                            amount.setScale(2, RoundingMode.HALF_UP), maximum));
        }
    }

    /**
     * 90% of the balance, rounded DOWN to the cent.
     *
     * DOWN rather than HALF_UP: rounding up could produce a stated maximum that
     * this very rule then rejects.
     */
    public BigDecimal calculateMaximumWithdrawal(BigDecimal balance) {
        return balance.multiply(WithdrawalRules.MAX_WITHDRAWAL_RATE)
                .setScale(2, RoundingMode.DOWN);
    }
}