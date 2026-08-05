package com.enviro.assessment.junior.bonganimoche.entity.enums;

/**
 * Lifecycle state of a withdrawal notice.
 * The current scope creates notices as COMPLETED immediately, but the enum
 * leaves room for an approval workflow without a schema change.
 */
public enum WithdrawalStatus {
    PENDING,
    COMPLETED,
    REJECTED
}