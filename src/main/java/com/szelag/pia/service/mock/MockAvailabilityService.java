package com.szelag.pia.service.mock;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.szelag.pia.model.ProductAvailability;
import com.szelag.pia.service.AvailabilityService;

/**
 * Mock implementation of AvailabilityService.
 * Simulates typical 100ms latency and 98% reliability for resilience testing.
 */
@Service
public class MockAvailabilityService extends AbstractMockService implements AvailabilityService {

        private static final long TYPICAL_LATENCY_MS = 100;
        private static final double RELIABILITY_PERCENT = 98.0;

        private static final Map<String, String> MARKET_WAREHOUSES = Map.of(
                        "nl-NL", "Rotterdam, NL",
                        "de-DE", "Hamburg, DE",
                        "pl-PL", "Poznań, PL",
                        "en-GB", "Birmingham, GB",
                        "fr-FR", "Lyon, FR");

        private static final Map<String, Integer> STOCK_LEVELS = Map.of(
                        "BOLT-M8-50", 4850,
                        "FILTER-HYD-001", 127,
                        "BEARING-6205-2RS", 43);

        @Override
        public ProductAvailability getAvailability(String productId, String marketCode) {
                simulateUpstreamCall(TYPICAL_LATENCY_MS, RELIABILITY_PERCENT, "AvailabilityService");

                int stock = STOCK_LEVELS.getOrDefault(productId, 0);
                String warehouse = MARKET_WAREHOUSES.getOrDefault(marketCode, "Central Warehouse, DE");

                // Approximate lead times: in-stock items ship within 2 days;
                // out-of-stock items require a 14-day replenishment window.
                LocalDate expectedDelivery = stock > 0
                                ? LocalDate.now().plusDays(2)
                                : LocalDate.now().plusDays(14);

                return new ProductAvailability(
                                stock,
                                warehouse,
                                expectedDelivery,
                                stock > 0);
        }
}