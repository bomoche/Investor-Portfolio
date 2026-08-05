package com.enviro.assessment.junior.bonganimoche.mapper;

import com.enviro.assessment.junior.bonganimoche.dto.response.PortfolioResponse;
import com.enviro.assessment.junior.bonganimoche.dto.response.ProductResponse;
import com.enviro.assessment.junior.bonganimoche.entity.Investor;
import com.enviro.assessment.junior.bonganimoche.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Converts domain entities into API response DTOs.
 *
 * Kept as its own Spring bean rather than living in the service so that mapping
 * has one home and can be unit tested in isolation. Written by hand rather than
 * generated with MapStruct — the object graph is small enough that an extra
 * annotation processor would cost more than it saves.
 */
@Component
public class PortfolioMapper {

    /** Business rule: a withdrawal may not exceed 90% of the product balance. */
    private static final BigDecimal MAX_WITHDRAWAL_RATE = new BigDecimal("0.90");

    public ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getProductType(),
                product.getCurrentBalance(),
                calculateMaximumWithdrawal(product.getCurrentBalance())
        );
    }

    public PortfolioResponse toPortfolioResponse(Investor investor) {
        List<ProductResponse> products = investor.getProducts().stream()
                .map(this::toProductResponse)
                .toList();

        BigDecimal total = products.stream()
                .map(ProductResponse::currentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PortfolioResponse(
                investor.getId(),
                investor.getFullName(),
                investor.getEmail(),
                investor.getAge(),
                total,
                products
        );
    }

    /**
     * 90% of the balance, rounded DOWN to the cent.
     *
     * RoundingMode.DOWN matters: rounding up could advertise a maximum that the
     * validation rule then rejects, so the ceiling shown always passes.
     */
    private BigDecimal calculateMaximumWithdrawal(BigDecimal balance) {
        return balance.multiply(MAX_WITHDRAWAL_RATE).setScale(2, RoundingMode.DOWN);
    }
}