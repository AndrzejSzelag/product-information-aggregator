package com.szelag.pia.model;

import java.util.List;
import java.util.Map;

/**
 * Core product data from the Catalog domain.
 * This is the "hard dependency" model required for any valid response.
 */
public record ProductCatalog(
        String id,
        String name,
        String description,
        String category,
        Map<String, String> specifications,
        List<String> imageUrls
) {}