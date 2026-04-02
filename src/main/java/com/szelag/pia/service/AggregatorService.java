package com.szelag.pia.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.szelag.pia.model.CustomerContext;
import com.szelag.pia.model.ProductAvailability;
import com.szelag.pia.model.ProductCatalog;
import com.szelag.pia.model.ProductPricing;
import com.szelag.pia.model.response.AggregatedProductResponse;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates parallel retrieval of product data from upstream services.
 *
 * <p>
 * Execution model:
 * <ul>
 * <li>Optional services (Pricing, Availability, Customer) are fired
 * concurrently via virtual threads. Failures degrade gracefully — the response
 * is still returned with the available subset of data.</li>
 * <li>CatalogService is mandatory and called on the calling thread. Any
 * exception propagates immediately to the GlobalExceptionHandler.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Latency: bounded by {@code max(individual call latencies)} rather than their
 * sum.
 * </p>
 */
@Slf4j
@Service
public class AggregatorService {

    private final CatalogService catalogService;
    private final PricingService pricingService;
    private final AvailabilityService availabilityService;
    private final CustomerService customerService;
    private final ExecutorService executor;

    /**
     * Production constructor — used by Spring. Creates a virtual-thread
     * executor for lightweight upstream I/O calls.
     */
    @Autowired // marks the runtime constructor for Spring dependency injection
    public AggregatorService(
            CatalogService catalogService,
            PricingService pricingService,
            AvailabilityService availabilityService,
            CustomerService customerService) {
        this(catalogService, pricingService, availabilityService, customerService,
                Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Package-private constructor for testing. Accepts a caller-supplied
     * executor so tests can use a same-thread executor and avoid
     * non-deterministic thread scheduling.
     */
    AggregatorService(
            CatalogService catalogService,
            PricingService pricingService,
            AvailabilityService availabilityService,
            CustomerService customerService,
            ExecutorService executor) {
        this.catalogService = catalogService;
        this.pricingService = pricingService;
        this.availabilityService = availabilityService;
        this.customerService = customerService;
        this.executor = executor;
    }

    @PreDestroy
    @SuppressWarnings("unused") // invoked by Spring via reflection on application context shutdown
    void shutdownExecutor() {
        executor.shutdown();
    }

    public AggregatedProductResponse aggregate(String productId, String marketCode, String customerId) {
        log.info("Starting aggregation: product={}, market={}, customer={}", productId, marketCode, customerId);

        // Optional services run concurrently; failures degrade gracefully.
        CompletableFuture<ProductPricing> pricingFuture = CompletableFuture.supplyAsync(
                () -> pricingService.getPricing(productId, marketCode, customerId), executor);

        CompletableFuture<ProductAvailability> availabilityFuture = CompletableFuture.supplyAsync(
                () -> availabilityService.getAvailability(productId, marketCode), executor);

        CompletableFuture<CustomerContext> customerFuture = customerId == null
                ? CompletableFuture.completedFuture(null)
                : CompletableFuture.supplyAsync(() -> customerService.getCustomer(customerId), executor);

        // Mandatory: propagates ProductNotFoundException / CatalogServiceException to
        // GlobalExceptionHandler.
        ProductCatalog product = catalogService.getProduct(productId, marketCode);

        PartialResult<ProductPricing> pricing = collect(pricingFuture, "PricingService");
        PartialResult<ProductAvailability> availability = collect(availabilityFuture, "AvailabilityService");
        PartialResult<CustomerContext> customer = collect(customerFuture, "CustomerService");

        return AggregatedProductResponse.builder()
                .product(product)
                .pricing(pricing.value())
                .availability(availability.value())
                .customer(customer.value())
                .marketCode(marketCode)
                .language(extractLanguage(marketCode))
                .dataAvailability(AggregatedProductResponse.DataAvailability.builder()
                        .pricingAvailable(pricing.available())
                        .availabilityAvailable(availability.available())
                        .customerContextAvailable(customerId != null && customer.available())
                        .pricingUnavailableReason(pricing.failureReason())
                        .availabilityUnavailableReason(availability.failureReason())
                        .customerContextUnavailableReason(customer.failureReason())
                        .build())
                .build();
    }

    /**
     * Joins an async future and wraps the result in a {@link PartialResult}.
     * Exceptions are caught and recorded as failures instead of breaking the
     * entire aggregation.
     */
    private <T> PartialResult<T> collect(CompletableFuture<T> future, String serviceName) {
        try {
            return PartialResult.success(future.join());
        } catch (CompletionException ex) {
            String errorMsg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            log.warn("{} failed: {}", serviceName, errorMsg);
            return PartialResult.failure(errorMsg);
        }
    }

    private String extractLanguage(String marketCode) {
        if (marketCode == null) {
            return null;
        }
        int idx = marketCode.indexOf('-');
        return idx > 0 ? marketCode.substring(0, idx) : marketCode;
    }

    /**
     * Carries the result of an optional upstream call. Either holds a non-null
     * value (success) or a failure reason (degraded).
     */
    private record PartialResult<T>(T value, boolean available, String failureReason) {

        // Treat null return as failure — prevents silent degradation masking.
        static <T> PartialResult<T> success(T value) {
            if (value == null) {
                return failure("Service returned null unexpectedly");
            }
            return new PartialResult<>(value, true, null);
        }

        static <T> PartialResult<T> failure(String reason) {
            return new PartialResult<>(null, false, reason);
        }
    }
}
