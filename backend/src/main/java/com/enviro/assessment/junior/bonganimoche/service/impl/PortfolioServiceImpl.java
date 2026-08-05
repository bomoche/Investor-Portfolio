package com.enviro.assessment.junior.bonganimoche.service.impl;

import com.enviro.assessment.junior.bonganimoche.dto.response.PortfolioResponse;
import com.enviro.assessment.junior.bonganimoche.entity.Investor;
import com.enviro.assessment.junior.bonganimoche.mapper.PortfolioMapper;
import com.enviro.assessment.junior.bonganimoche.repository.InvestorRepository;
import com.enviro.assessment.junior.bonganimoche.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final InvestorRepository investorRepository;
    private final PortfolioMapper portfolioMapper;

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
}