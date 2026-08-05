package com.enviro.assessment.junior.bonganimoche.repository;

import com.enviro.assessment.junior.bonganimoche.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByInvestorId(Long investorId);

    /**
     * Ownership-scoped lookup. Fetching by product id alone would let one
     * investor withdraw from another's product simply by guessing an id, so
     * the service always resolves products through this method.
     */
    Optional<Product> findByIdAndInvestorId(Long id, Long investorId);
}