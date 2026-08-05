package com.enviro.assessment.junior.bonganimoche.service;

import com.enviro.assessment.junior.bonganimoche.dto.response.PortfolioResponse;

/**
 * Read operations over an investor's portfolio.
 *
 * An interface with a separate implementation so controllers depend on the
 * abstraction rather than the concrete class — which also lets the controller
 * tests mock this cleanly without a database.
 */
public interface PortfolioService {

    PortfolioResponse getPortfolio(Long investorId);
}