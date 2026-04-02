package com.szelag.pia.service;

import com.szelag.pia.model.ProductAvailability;

/**
 * Provides real-time stock levels and estimated delivery information.
 *
 * <p>Optional dependency — unavailability degrades the response gracefully;
 * the aggregated result is still returned without availability data.
 */
public interface AvailabilityService {

    /**
     * Returns a stock and delivery snapshot for the given product and market.
     *
     * @param productId  Product identifier (e.g., BOLT-M8-50).
     * @param marketCode BCP-47 market code; determines relevant warehouse locations.
     * @return Availability snapshot with stock level, warehouse, and expected delivery.
     */
    ProductAvailability getAvailability(String productId, String marketCode);
}