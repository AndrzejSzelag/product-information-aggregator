package com.szelag.pia.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.szelag.pia.exception.CatalogServiceException;
import com.szelag.pia.exception.ProductNotFoundException;
import com.szelag.pia.model.CustomerContext;
import com.szelag.pia.model.ProductAvailability;
import com.szelag.pia.model.ProductCatalog;
import com.szelag.pia.model.ProductPricing;
import com.szelag.pia.model.response.AggregatedProductResponse;

/**
 * Tests cover the orchestration logic of AggregatorService:
 * parallel execution with graceful degradation, hard vs soft dependency
 * contract, and anonymous user path. Mocks are not tested here.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AggregatorService — unit tests")
class AggregatorServiceTest {

    @Mock
    private CatalogService catalogService;
    @Mock
    private PricingService pricingService;
    @Mock
    private AvailabilityService availabilityService;
    @Mock
    private CustomerService customerService;

    // Not @InjectMocks — we need to supply a controlled executor (see setUp()).
    private AggregatorService aggregatorService;

    /**
     * Executes every submitted task on the calling thread, synchronously.
     * This makes CompletableFuture.supplyAsync() behave like a direct call,
     * keeping tests deterministic without spawning real threads.
     */
    private static final ExecutorService SAME_THREAD_EXECUTOR = new AbstractExecutorService() {
        @Override
        public void execute(Runnable r) {
            r.run();
        }

        @Override
        public void shutdown() {
        }

        @Override
        public List<Runnable> shutdownNow() {
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }
    };

    @BeforeEach
    void setUp() {
        aggregatorService = new AggregatorService(
                catalogService,
                pricingService,
                availabilityService,
                customerService,
                SAME_THREAD_EXECUTOR);
    }

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String PRODUCT_ID = "BOLT-M8-50";
    private static final String MARKET_CODE = "pl-PL";
    private static final String CUSTOMER_ID = "CUSTOMER-001";

    private static final ProductCatalog STUB_CATALOG = new ProductCatalog(
            PRODUCT_ID,
            "Śruba M8x50 ocynkowana",
            "Wysokiej jakości śruba ze stali ocynkowanej, metryczna M8, długość 50mm.",
            "Fasteners",
            Map.of("thread", "M8", "length_mm", "50", "material", "Steel"),
            List.of(
                    "https://cdn.com.szelag/products/BOLT-M8-50/main.jpg",
                    "https://cdn.com.szelag/products/BOLT-M8-50/detail.jpg"));

    // base: 0.45 EUR * 4.25 PLN = 1.91 PLN, discount PREMIUM=0.15, final=1.62 PLN
    private static final ProductPricing STUB_PRICING = new ProductPricing(
            new BigDecimal("1.91"),
            new BigDecimal("0.15"),
            new BigDecimal("1.62"),
            "PLN");

    private static final CustomerContext STUB_CUSTOMER = new CustomerContext(
            "CUSTOMER-001",
            "PREMIUM",
            List.of("hydraulics", "fasteners"),
            List.of());

    // Contains LocalDate.now() — cannot be static final
    private ProductAvailability stubAvailability() {
        return new ProductAvailability(
                4850,
                "Poznań, PL",
                LocalDate.now().plusDays(2),
                true);
    }

    // Explicit helper — avoids UnnecessaryStubbingException in strict Mockito mode.
    // Called only in tests that need all four services to succeed.
    private void stubAllServicesOk() {
        when(catalogService.getProduct(PRODUCT_ID, MARKET_CODE)).thenReturn(STUB_CATALOG);
        when(pricingService.getPricing(PRODUCT_ID, MARKET_CODE, CUSTOMER_ID)).thenReturn(STUB_PRICING);
        when(availabilityService.getAvailability(PRODUCT_ID, MARKET_CODE)).thenReturn(stubAvailability());
        when(customerService.getCustomer(CUSTOMER_ID)).thenReturn(STUB_CUSTOMER);
    }

    // =========================================================================
    // Happy path
    // =========================================================================

    @Nested
    @DisplayName("Happy path — all services succeed")
    class HappyPath {

        @Test
        @DisplayName("Returns full response with all fields populated")
        void aggregate_allServicesOk_returnsFullResponse() {
            stubAllServicesOk();

            AggregatedProductResponse response = aggregatorService.aggregate(PRODUCT_ID, MARKET_CODE, CUSTOMER_ID);

            assertThat(response.product()).isEqualTo(STUB_CATALOG);
            assertThat(response.pricing()).isEqualTo(STUB_PRICING);
            assertThat(response.customer()).isEqualTo(STUB_CUSTOMER);
            assertThat(response.availability().stockLevel()).isEqualTo(4850);
            assertThat(response.availability().warehouseLocation()).isEqualTo("Poznań, PL");

            AggregatedProductResponse.DataAvailability da = response.dataAvailability();
            assertThat(da.pricingAvailable()).isTrue();
            assertThat(da.availabilityAvailable()).isTrue();
            assertThat(da.customerContextAvailable()).isTrue();
        }

        @Test
        @DisplayName("Extracts language code correctly from compound market code")
        void aggregate_extractsLanguageFromMarketCode() {
            when(catalogService.getProduct(PRODUCT_ID, "nl-NL")).thenReturn(STUB_CATALOG);
            when(pricingService.getPricing(eq(PRODUCT_ID), eq("nl-NL"), eq(CUSTOMER_ID))).thenReturn(STUB_PRICING);
            when(availabilityService.getAvailability(eq(PRODUCT_ID), eq("nl-NL"))).thenReturn(stubAvailability());
            when(customerService.getCustomer(CUSTOMER_ID)).thenReturn(STUB_CUSTOMER);

            AggregatedProductResponse response = aggregatorService.aggregate(PRODUCT_ID, "nl-NL", CUSTOMER_ID);

            assertThat(response.language()).isEqualTo("nl");
        }
    }

    // =========================================================================
    // Graceful degradation — optional services fail
    // =========================================================================

    @Nested
    @DisplayName("Graceful degradation — optional services fail")
    class GracefulDegradation {

        @Test
        @DisplayName("Returns partial response when PricingService fails")
        void aggregate_pricingFails_returnsPartialResponse() {
            when(catalogService.getProduct(PRODUCT_ID, MARKET_CODE)).thenReturn(STUB_CATALOG);
            when(pricingService.getPricing(any(), any(), any()))
                    .thenThrow(new RuntimeException("Pricing timeout"));
            when(availabilityService.getAvailability(PRODUCT_ID, MARKET_CODE)).thenReturn(stubAvailability());
            when(customerService.getCustomer(CUSTOMER_ID)).thenReturn(STUB_CUSTOMER);

            AggregatedProductResponse response = aggregatorService.aggregate(PRODUCT_ID, MARKET_CODE, CUSTOMER_ID);

            assertThat(response.pricing()).isNull();
            assertThat(response.dataAvailability().pricingAvailable()).isFalse();
            assertThat(response.dataAvailability().pricingUnavailableReason()).contains("Pricing timeout");
            assertThat(response.product()).isEqualTo(STUB_CATALOG);
            assertThat(response.availability().stockLevel()).isEqualTo(4850);
        }

        @Test
        @DisplayName("Returns partial response when AvailabilityService fails")
        void aggregate_availabilityFails_returnsPartialResponse() {
            when(catalogService.getProduct(PRODUCT_ID, MARKET_CODE)).thenReturn(STUB_CATALOG);
            when(pricingService.getPricing(PRODUCT_ID, MARKET_CODE, CUSTOMER_ID)).thenReturn(STUB_PRICING);
            when(availabilityService.getAvailability(any(), any()))
                    .thenThrow(new RuntimeException("Warehouse API down"));
            when(customerService.getCustomer(CUSTOMER_ID)).thenReturn(STUB_CUSTOMER);

            AggregatedProductResponse response = aggregatorService.aggregate(PRODUCT_ID, MARKET_CODE, CUSTOMER_ID);

            assertThat(response.availability()).isNull();
            assertThat(response.dataAvailability().availabilityAvailable()).isFalse();
            assertThat(response.dataAvailability().availabilityUnavailableReason()).contains("Warehouse API down");
            assertThat(response.product()).isEqualTo(STUB_CATALOG);
            assertThat(response.pricing()).isEqualTo(STUB_PRICING);
        }

        @Test
        @DisplayName("Returns partial response when CustomerService fails")
        void aggregate_customerFails_returnsPartialResponse() {
            when(catalogService.getProduct(PRODUCT_ID, MARKET_CODE)).thenReturn(STUB_CATALOG);
            when(pricingService.getPricing(PRODUCT_ID, MARKET_CODE, CUSTOMER_ID)).thenReturn(STUB_PRICING);
            when(availabilityService.getAvailability(PRODUCT_ID, MARKET_CODE)).thenReturn(stubAvailability());
            when(customerService.getCustomer(any()))
                    .thenThrow(new RuntimeException("Customer service down"));

            AggregatedProductResponse response = aggregatorService.aggregate(PRODUCT_ID, MARKET_CODE, CUSTOMER_ID);

            assertThat(response.customer()).isNull();
            assertThat(response.dataAvailability().customerContextAvailable()).isFalse();
            assertThat(response.product()).isEqualTo(STUB_CATALOG);
            assertThat(response.pricing()).isEqualTo(STUB_PRICING);
        }

        @Test
        @DisplayName("Catalog is still returned when all optional services fail simultaneously")
        void aggregate_allOptionalServicesFail_catalogStillReturned() {
            when(catalogService.getProduct(PRODUCT_ID, MARKET_CODE)).thenReturn(STUB_CATALOG);
            when(pricingService.getPricing(any(), any(), any()))
                    .thenThrow(new RuntimeException("Pricing down"));
            when(availabilityService.getAvailability(any(), any()))
                    .thenThrow(new RuntimeException("Availability down"));
            when(customerService.getCustomer(any()))
                    .thenThrow(new RuntimeException("Customer down"));

            AggregatedProductResponse response = aggregatorService.aggregate(PRODUCT_ID, MARKET_CODE, CUSTOMER_ID);

            assertThat(response.product()).isEqualTo(STUB_CATALOG);
            assertThat(response.pricing()).isNull();
            assertThat(response.availability()).isNull();
            assertThat(response.customer()).isNull();

            AggregatedProductResponse.DataAvailability da = response.dataAvailability();
            assertThat(da.pricingAvailable()).isFalse();
            assertThat(da.availabilityAvailable()).isFalse();
            assertThat(da.customerContextAvailable()).isFalse();
        }
    }

    // =========================================================================
    // Hard dependency — CatalogService failures must propagate
    // =========================================================================

    @Nested
    @DisplayName("Hard dependency — CatalogService failures propagate")
    class CatalogHardDependency {

        @Test
        @DisplayName("Throws ProductNotFoundException when catalog returns 404")
        void aggregate_catalogNotFound_throwsProductNotFoundException() {
            when(catalogService.getProduct(PRODUCT_ID, MARKET_CODE))
                    .thenThrow(new ProductNotFoundException("Product not found: " + PRODUCT_ID));

            assertThatThrownBy(() -> aggregatorService.aggregate(PRODUCT_ID, MARKET_CODE, CUSTOMER_ID))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining(PRODUCT_ID);

            verify(catalogService).getProduct(PRODUCT_ID, MARKET_CODE);
        }

        @Test
        @DisplayName("Throws CatalogServiceException when catalog is unavailable")
        void aggregate_catalogUnavailable_throwsCatalogServiceException() {
            when(catalogService.getProduct(PRODUCT_ID, MARKET_CODE))
                    .thenThrow(new CatalogServiceException(
                            "Catalog service timeout",
                            new RuntimeException("connection refused")));

            assertThatThrownBy(() -> aggregatorService.aggregate(PRODUCT_ID, MARKET_CODE, CUSTOMER_ID))
                    .isInstanceOf(CatalogServiceException.class)
                    .hasMessageContaining("Catalog service timeout");
        }
    }

    // =========================================================================
    // Anonymous user — no customerId
    // =========================================================================

    @Nested
    @DisplayName("Anonymous user — customerId is null")
    class AnonymousUser {

        @Test
        @DisplayName("Skips CustomerService and marks customerContext unavailable when customerId is null")
        void aggregate_noCustomerId_skipsCustomerServiceAndMarksUnavailable() {
            when(catalogService.getProduct(PRODUCT_ID, MARKET_CODE)).thenReturn(STUB_CATALOG);
            when(pricingService.getPricing(PRODUCT_ID, MARKET_CODE, null)).thenReturn(STUB_PRICING);
            when(availabilityService.getAvailability(PRODUCT_ID, MARKET_CODE)).thenReturn(stubAvailability());

            AggregatedProductResponse response = aggregatorService.aggregate(PRODUCT_ID, MARKET_CODE, null);

            assertThat(response.customer()).isNull();
            assertThat(response.dataAvailability().customerContextAvailable()).isFalse();
            verifyNoInteractions(customerService);
        }
    }
}