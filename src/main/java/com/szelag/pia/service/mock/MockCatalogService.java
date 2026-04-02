package com.szelag.pia.service.mock;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.szelag.pia.exception.CatalogServiceException;
import com.szelag.pia.exception.ProductNotFoundException;
import com.szelag.pia.model.ProductCatalog;
import com.szelag.pia.service.CatalogService;

/**
 * Mock implementation of CatalogService.
 * High reliability (99.9%) reflecting its mandatory role in the aggregation.
 */
@Service
public class MockCatalogService extends AbstractMockService implements CatalogService {

    private static final long   TYPICAL_LATENCY_MS  = 50;
    private static final double RELIABILITY_PERCENT  = 99.9;

    private static final String DEFAULT_IMAGE_URL    =
        "https://images.unsplash.com/photo-1621905251918-48416bd8575a?auto=format&fit=crop&w=200";

    @Override
    public ProductCatalog getProduct(String productId, String marketCode) {
        try {
            simulateUpstreamCall(TYPICAL_LATENCY_MS, RELIABILITY_PERCENT, "CatalogService");
        } catch (RuntimeException ex) {
            throw new CatalogServiceException("Catalog failed for product: " + productId, ex);
        }

        if (!ProductData.NAMES.containsKey(productId)) {
            throw new ProductNotFoundException(productId);
        }

        String language = extractLanguage(marketCode);
        String name     = resolveLocalised(ProductData.NAMES, productId, marketCode, language, productId);
        List<String> imageUrls = ProductData.IMAGES.getOrDefault(productId, List.of(DEFAULT_IMAGE_URL));

        return new ProductCatalog(
            productId,
            name,
            resolveLocalised(ProductData.DESCRIPTIONS, productId, marketCode, language, "Product " + productId),
            ProductCategory.from(productId).getLabel(),
            ProductData.SPECS.getOrDefault(productId, Map.of()),
            imageUrls);
    }

    /**
     * Extracts the ISO 639-1 language code from a BCP-47 market code.
     * Examples: "nl-NL" → "nl", "pl-PL" → "pl", "en" → "en".
     */
    private String extractLanguage(String marketCode) {
        return marketCode.contains("-") ? marketCode.split("-")[0] : marketCode;
    }

    /**
     * Resolves a localised string with a three-level fallback:
     * 1. exact marketCode  (e.g. "nl-NL")
     * 2. language + "-GB"  (e.g. "nl-GB")
     * 3. "en-GB"
     * 4. provided default
     */
    private String resolveLocalised(
            Map<String, Map<String, String>> dataMap,
            String productId,
            String marketCode,
            String language,
            String fallback) {

        Map<String, String> entries = dataMap.get(productId);
        if (entries == null) return fallback;

        return entries.getOrDefault(marketCode,
               entries.getOrDefault(language + "-GB",
               entries.getOrDefault("en-GB", fallback)));
    }
}