package com.szelag.pia.service;

import com.szelag.pia.model.ProductPricing;

/**
 * Provides product pricing with currency conversion and customer-specific discounts.
 *
 * <p>Optional dependency — unavailability results in a "price unavailable" state
 * in the aggregated response rather than a request failure.
 */
public interface PricingService {

    /**
     * Returns a pricing snapshot for the given product, market, and customer context.
     *
     * @param productId  Product identifier (e.g., BOLT-M8-50).
     * @param marketCode BCP-47 market code; determines currency and base market price.
     * @param customerId Optional customer ID; if present, triggers customer-specific discounts.
     * @return Pricing snapshot with base price, discount rate, final price, and currency.
     */
    ProductPricing getPricing(String productId, String marketCode, String customerId);
}