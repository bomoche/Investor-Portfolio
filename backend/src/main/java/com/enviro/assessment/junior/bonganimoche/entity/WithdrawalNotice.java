package com.enviro.assessment.junior.bonganimoche.entity;

import com.enviro.assessment.junior.bonganimoche.entity.enums.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A record of an investor's request to withdraw funds from a product.
 *
 * balanceBefore and balanceAfter are point-in-time snapshots, deliberately
 * duplicated from Product.currentBalance. Because the balance on Product is
 * mutable, a statement generated months later would otherwise show today's
 * balance against a historical withdrawal. Snapshotting keeps exported
 * statements accurate and auditable.
 */
@Entity
@Table(name = "withdrawal_notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable reference shown in the UI and CSV export. */
    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceBefore;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WithdrawalStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @PrePersist
    protected void onCreate() {
        this.requestedAt = LocalDateTime.now();
    }
}