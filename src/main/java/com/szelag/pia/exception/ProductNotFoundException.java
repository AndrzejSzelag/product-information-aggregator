package com.szelag.pia.exception;

/**
 * Thrown when a valid request is made but the product ID does not exist.
 * Mapped to 404 Not Found.
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("Product not found: " + productId);
    }
}