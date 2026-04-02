package com.szelag.pia.service.mock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.szelag.pia.model.ProductPricing;
import com.szelag.pia.service.PricingService;

/**
 * Mock implementation of PricingService.
 * Simulates 80ms latency and 99.5% reliability.
 * Applies market-specific currency conversion and customer-tier discounts.
 */
@Service
public class MockPricingService extends AbstractMockService implements PricingService {

    private static final long TYPICAL_LATENCY_MS = 80;
    private static final double RELIABILITY_PERCENT = 99.5;

    private static final Map<String, BigDecimal> BASE_PRICES = Map.of(
            "BOLT-M8-50", new BigDecimal("0.45"),
            "FILTER-HYD-001", new BigDecimal("28.90"),
            "BEARING-6205-2RS", new BigDecimal("12.50"));

    private static final Map<String, String> MARKET_CURRENCIES = Map.of(
            "nl-NL", "EUR",
            "pl-PL", "PLN",
            "en-GB", "GBP");

    private static final Map<String, BigDecimal> CONVERSION_RATES = Map.of(
            "EUR", BigDecimal.ONE,
            "PLN", new BigDecimal("4.25"),
            "GBP", new BigDecimal("0.86"));

    @Override
    public ProductPricing getPricing(String productId, String marketCode, String customerId) {
        simulateUpstreamCall(TYPICAL_LATENCY_MS, RELIABILITY_PERCENT, "PricingService");

        BigDecimal baseEur = BASE_PRICES.getOrDefault(productId, new BigDecimal("9.99"));
        String currency = MARKET_CURRENCIES.getOrDefault(marketCode, "EUR");
        BigDecimal rate = CONVERSION_RATES.getOrDefault(currency, BigDecimal.ONE);

        BigDecimal basePrice = baseEur.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = resolveDiscount(customerId);
        // BigDecimal used throughout for financial precision — avoids floating-point
        // rounding errors.
        BigDecimal finalPrice = basePrice.subtract(basePrice.multiply(discount))
                .setScale(2, RoundingMode.HALF_UP);

        return new ProductPricing(basePrice, discount, finalPrice, currency);
    }

    /**
     * Resolves a discount rate by customer tier.
     * Discount tier is derived from the customerId suffix — mock shortcut;
     * production would look up the segment from CustomerService.
     */
    private BigDecimal resolveDiscount(String customerId) {
        if (customerId == null) {
            return BigDecimal.ZERO;
        }
        if (customerId.endsWith("-001")) {
            return new BigDecimal("0.15"); // PREMIUM
        }
        if (customerId.endsWith("-002")) {
            return new BigDecimal("0.20"); // WHOLESALE
        }
        return new BigDecimal("0.05"); // STANDARD
    }
}