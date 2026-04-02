package com.szelag.pia.service.mock;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.szelag.pia.model.CustomerContext;
import com.szelag.pia.service.CustomerService;

/**
 * Mock implementation of CustomerService.
 * Typical latency: 60ms, reliability: 99.0%.
 */
@Service
public class MockCustomerService extends AbstractMockService implements CustomerService {

    private static final long TYPICAL_LATENCY_MS = 60;
    private static final double RELIABILITY_PERCENT = 99.0;

    // In-memory customer fixtures; keyed by customerId for O(1) lookup.
    private static final Map<String, CustomerContext> CUSTOMERS = Map.of(
            "CUSTOMER-001", new CustomerContext(
                    "CUSTOMER-001",
                    "PREMIUM",
                    List.of("hydraulics", "fasteners"),
                    List.of("P-1001", "P-2002")),
            "CUSTOMER-002", new CustomerContext(
                    "CUSTOMER-002",
                    "WHOLESALE",
                    List.of("bearings", "seals"),
                    List.of("P-3003")),
            "CUSTOMER-003", new CustomerContext(
                    "CUSTOMER-003",
                    "STANDARD",
                    List.of("general"),
                    List.of()));

    @Override
    public CustomerContext getCustomer(String customerId) {
        simulateUpstreamCall(TYPICAL_LATENCY_MS, RELIABILITY_PERCENT, "CustomerService");

        // Unknown customers receive a STANDARD profile;
        // in production a 404 would be treated as a soft failure.
        return CUSTOMERS.getOrDefault(customerId,
                new CustomerContext(customerId, "STANDARD", List.of(), List.of()));
    }
}