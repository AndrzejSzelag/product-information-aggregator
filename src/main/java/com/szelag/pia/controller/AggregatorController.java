package com.szelag.pia.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.szelag.pia.model.response.AggregatedProductResponse;
import com.szelag.pia.service.AggregatorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST entry point for product data aggregation.
 * Delegates all business logic to {@link AggregatorService} to keep the
 * controller lean.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class AggregatorController {

    private final AggregatorService aggregatorService;

    /**
     * Returns aggregated product data from Catalog, Pricing, Availability,
     * and Customer services in a single response.
     *
     * <p>
     * Optional services degrade gracefully — if Pricing or Availability
     * are unavailable, the response is still returned with partial data.
     *
     * @param productId  Product identifier (e.g., BOLT-M8-50).
     * @param market     BCP-47 market code (e.g., pl-PL, nl-NL).
     * @param customerId Optional customer ID; if absent, anonymous pricing applies.
     * @return 200 with aggregated data; 404 if product not found; 503 if catalog
     *         unavailable.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<AggregatedProductResponse> getProduct(
            @PathVariable String productId,
            @RequestParam String market,
            @RequestParam(required = false) String customerId) {

        log.debug("API request: productId={}, market={}, customerId={}", productId, market, customerId);

        AggregatedProductResponse response = aggregatorService.aggregate(productId, market, customerId);
        return ResponseEntity.ok(response);
    }
}