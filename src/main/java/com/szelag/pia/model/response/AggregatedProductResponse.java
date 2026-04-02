package com.szelag.pia.model.response;

import com.szelag.pia.model.CustomerContext;
import com.szelag.pia.model.ProductAvailability;
import com.szelag.pia.model.ProductCatalog;
import com.szelag.pia.model.ProductPricing;

import lombok.Builder;

/**
 * Final aggregated view. Uses Java Records for immutability and Lombok @Builder
 * for flexible assembly. Critical for thread-safety when combining results from
 * multiple CompletableFutures.
 */
@Builder
public record AggregatedProductResponse(
        // Required: basic product info
        ProductCatalog product,
        // Optional: may be null if upstream services fail
        ProductPricing pricing,
        ProductAvailability availability,
        CustomerContext customer,
        // Metadata for graceful degradation handling on the frontend
        DataAvailability dataAvailability,
        // Contextual information
        String marketCode,
        String language) {

    /**
     * Indicators for partial response handling.
     */
    @Builder
    public record DataAvailability(
            boolean pricingAvailable,
            boolean availabilityAvailable,
            boolean customerContextAvailable,
            String pricingUnavailableReason,
            String availabilityUnavailableReason,
            String customerContextUnavailableReason) {
    }
}
