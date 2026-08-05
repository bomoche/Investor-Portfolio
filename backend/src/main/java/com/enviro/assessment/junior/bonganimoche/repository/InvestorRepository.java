package com.enviro.assessment.junior.bonganimoche.repository;

import com.enviro.assessment.junior.bonganimoche.entity.Investor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvestorRepository extends JpaRepository<Investor, Long> {

    /** Used by the authentication layer — email is the login identifier. */
    Optional<Investor> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Loads an investor together with their products in a single query.
     *
     * Products are LAZY, so the portfolio endpoint would otherwise issue one
     * query for the investor and a second when the collection is touched.
     * LEFT JOIN FETCH collapses that into one statement, and LEFT (rather than
     * inner) keeps investors with no products in the result.
     */
    @Query("SELECT i FROM Investor i LEFT JOIN FETCH i.products WHERE i.id = :id")
    Optional<Investor> findByIdWithProducts(@Param("id") Long id);
}