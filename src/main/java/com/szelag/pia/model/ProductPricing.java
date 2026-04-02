package com.szelag.pia.model;

import java.math.BigDecimal;

/**
 * Snapshot of product pricing.
 * Includes calculated discounts and final price for the specific market
 * context.
 */
public record ProductPricing(
        BigDecimal basePrice,
        BigDecimal customerDiscount,
        BigDecimal finalPrice,
        String currency
) {}