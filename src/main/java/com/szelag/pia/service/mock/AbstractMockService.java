package com.szelag.pia.service.mock;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Base for mock upstream services.
 * Simulates network latency (with jitter) and random failures
 * to test the resilience of the aggregator.
 */
abstract class AbstractMockService {

    private final Random random = new Random();

    /**
     * Simulates a network call with artificial latency and failure probability.
     *
     * @param typicalLatencyMs   Base latency in milliseconds; actual latency varies
     *                           by ±25% (jitter).
     * @param reliabilityPercent Success probability expressed as a percentage
     *                           (0.0–100.0).
     * @param serviceName        Name used in the exception message if the simulated
     *                           call fails.
     */
    protected void simulateUpstreamCall(long typicalLatencyMs, double reliabilityPercent, String serviceName) {
        // Jitter prevents all mock services from completing at the same time,
        // which would not reflect real network conditions.
        long jitter = (long) (typicalLatencyMs * 0.25);
        long latency = typicalLatencyMs + (random.nextLong(jitter * 2 + 1) - jitter);
        latency = Math.max(10, latency);

        try {
            // Restores interrupt flag on InterruptedException — standard Java concurrency practice.
            TimeUnit.MILLISECONDS.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(serviceName + " interrupted", e);
        }

        // Throws if random value exceeds the reliability threshold (e.g. 2% chance when reliability=98.0).
        if (random.nextDouble() > (reliabilityPercent / 100.0)) {
            throw new RuntimeException(serviceName + " failure (simulated)");
        }
    }
}