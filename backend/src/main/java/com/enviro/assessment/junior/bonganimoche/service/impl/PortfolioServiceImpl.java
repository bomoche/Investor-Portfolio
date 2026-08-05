package com.enviro.assessment.junior.bonganimoche.service.impl;

import com.enviro.assessment.junior.bonganimoche.dto.response.EligibilityResponse;
import com.enviro.assessment.junior.bonganimoche.dto.response.PortfolioResponse;
import com.enviro.assessment.junior.bonganimoche.entity.Investor;
import com.enviro.assessment.junior.bonganimoche.entity.Product;
import com.enviro.assessment.junior.bonganimoche.exception.BusinessRuleViolationException;
import com.enviro.assessment.junior.bonganimoche.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.bonganimoche.mapper.PortfolioMapper;
import com.enviro.assessment.junior.bonganimoche.repository.InvestorRepository;
import com.enviro.assessment.junior.bonganimoche.repository.ProductRepository;
import com.enviro.assessment.junior.bonganimoche.service.PortfolioService;
import com.enviro.assessment.junior.bonganimoche.service.WithdrawalValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final InvestorRepository investorRepository;
    private final PortfolioMapper portfolioMapper;
    private final ProductRepository productRepository;
    private final WithdrawalValidator withdrawalValidator;

    /**
     * readOnly = true tells Hibernate to skip dirty checking on the loaded
     * entities, since nothing here mutates state. It also keeps the entities
     * managed while the mapper walks the lazy products collection — without an
     * open transaction that traversal would throw LazyInitializationException.
     */
    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(Long investorId) {
        log.debug("Retrieving portfolio for investor {}", investorId);

        Investor investor = investorRepository.findByIdWithProducts(investorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Investor not found with id: " + investorId));

        return portfolioMapper.toPortfolioResponse(investor);
    }

    @Override
    @Transactional(readOnly = true)
    public EligibilityResponse getEligibility(Long investorId, Long productId) {
        Product product = productRepository.findByIdAndInvestorId(productId, investorId)
                .orElseThrow(() -> ResourceNotFoundException.forProduct(productId));

        BigDecimal maximum = withdrawalValidator.calculateMaximumWithdrawal(
                product.getCurrentBalance());

        // Reuses the same validator as the write path with a nominal amount, so
        // the eligibility shown can never drift from the rules enforced.
        try {
            withdrawalValidator.validate(product.getInvestor(), product, new BigDecimal("0.01"));
            return new EligibilityResponse(product.getId(), product.getProductName(),
                    true, product.getCurrentBalance(), maximum, null);
        } catch (BusinessRuleViolationException e) {
            return new EligibilityResponse(product.getId(), product.getProductName(),
                    false, product.getCurrentBalance(), BigDecimal.ZERO, e.getMessage());
        }
    }   
}