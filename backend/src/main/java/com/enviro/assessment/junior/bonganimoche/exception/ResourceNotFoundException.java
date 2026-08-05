package com.enviro.assessment.junior.bonganimoche.exception;

/**
 * Thrown when a requested entity does not exist, or exists but is not owned by
 * the requesting investor.
 *
 * Deliberately does not distinguish those two cases in its message: telling an
 * attacker that a product exists but belongs to someone else leaks information.
 * Both map to 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forProduct(Long productId) {
        return new ResourceNotFoundException("Product not found with id: " + productId);
    }

    public static ResourceNotFoundException forInvestor(Long investorId) {
        return new ResourceNotFoundException("Investor not found with id: " + investorId);
    }
}