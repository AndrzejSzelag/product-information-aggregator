package com.szelag.pia.service.mock;

import java.util.Arrays;

/**
 * enum-based category resolution instead of fragile startsWith() checks.
 * Each value declares which prefix it owns.
 */
public enum ProductCategory {

    FASTENERS("BOLT", "Fasteners"),
    FILTRATION("FILTER", "Filtration"),
    BEARINGS("BEARING", "Bearings"),
    GENERAL("", "General Parts");

    private final String prefix;
    private final String label;

    ProductCategory(String prefix, String label) {
        this.prefix = prefix;
        this.label = label;
    }

    /**
     * Resolves category from productId.
     * Falls back to GENERAL if no prefix matches.
     */
    public static ProductCategory from(String productId) {
        return Arrays.stream(values())
                .filter(c -> !c.prefix.isEmpty() && productId.startsWith(c.prefix))
                .findFirst()
                .orElse(GENERAL);
    }

    public String getLabel() {
        return label;
    }
}