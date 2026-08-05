package com.enviro.assessment.junior.bonganimoche.repository;

import com.enviro.assessment.junior.bonganimoche.entity.WithdrawalNotice;
import com.enviro.assessment.junior.bonganimoche.entity.enums.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WithdrawalNoticeRepository extends JpaRepository<WithdrawalNotice, Long> {

    /** Full history for one investor, newest first. */
    List<WithdrawalNotice> findByProductInvestorIdOrderByRequestedAtDesc(Long investorId);

    boolean existsByReference(String reference);

    /**
     * Filtered history backing both the history table and the CSV export.
     *
     * Every filter is optional: a null parameter short-circuits its condition
     * via the `:param IS NULL OR ...` idiom, so one query serves all filter
     * combinations rather than needing a Specification or several methods.
     */
    @Query("""
           SELECT w FROM WithdrawalNotice w
           JOIN w.product p
           WHERE p.investor.id = :investorId
             AND (:productId IS NULL OR p.id = :productId)
             AND (:status IS NULL OR w.status = :status)
             AND (:from IS NULL OR w.requestedAt >= :from)
             AND (:to IS NULL OR w.requestedAt <= :to)
           ORDER BY w.requestedAt DESC
           """)
    List<WithdrawalNotice> findFiltered(
            @Param("investorId") Long investorId,
            @Param("productId") Long productId,
            @Param("status") WithdrawalStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}