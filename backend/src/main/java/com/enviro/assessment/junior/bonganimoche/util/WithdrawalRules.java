package com.enviro.assessment.junior.bonganimoche.util;

import java.math.BigDecimal;

/**
 * Central home for the withdrawal thresholds and their error codes.
 *
 * Named constants rather than literals scattered through the service: the 90%
 * ceiling appears in the validator, the mapper's maximumWithdrawal calculation
 * and the eligibility endpoint, and all three must agree.
 */
public final class WithdrawalRules {

    private WithdrawalRules() {
        // Utility class — not instantiable.
    }

    /** Minimum age at which a retirement product may be drawn down. */
    public static final int RETIREMENT_MINIMUM_AGE = 65;

    /** Maximum proportion of a product balance withdrawable in one notice. */
    public static final BigDecimal MAX_WITHDRAWAL_RATE = new BigDecimal("0.90");

    public static final String CODE_RETIREMENT_AGE = "RETIREMENT_AGE_RESTRICTION";
    public static final String CODE_EXCEEDS_BALANCE = "EXCEEDS_AVAILABLE_BALANCE";
    public static final String CODE_EXCEEDS_LIMIT = "EXCEEDS_WITHDRAWAL_LIMIT";
}