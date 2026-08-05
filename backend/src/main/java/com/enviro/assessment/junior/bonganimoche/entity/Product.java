package com.enviro.assessment.junior.bonganimoche.entity;

import com.enviro.assessment.junior.bonganimoche.entity.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A single investment product held by an investor.
 *
 * currentBalance is a stored column updated inside the same transaction that
 * creates a withdrawal notice. The alternative — deriving it from the sum of
 * withdrawals on every read — avoids any risk of drift, but costs an aggregate
 * query per product on the dashboard. Given that all balance mutations flow
 * through a single transactional service method, the stored column is safe here
 * and keeps reads cheap.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType productType;

    /**
     * BigDecimal, not double — binary floating point cannot represent decimal
     * fractions like 0.10 exactly, which produces cent-level rounding errors
     * once amounts are summed. precision 19 / scale 2 stores ZAR to the cent.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance;

    /** Owning side of the relationship — holds the investor_id foreign key. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investor_id", nullable = false)
    private Investor investor;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WithdrawalNotice> withdrawalNotices = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}