package com.szelag.pia.service;

import com.szelag.pia.model.ProductCatalog;

/**
 * Provides core product data including localized names, descriptions, and specifications.
 *
 * <p>Mandatory dependency — any failure propagates immediately and results
 * in a 503 Service Unavailable response.
 */
public interface CatalogService {

    /**
     * Returns localized product catalog data for the given market.
     *
     * @param productId  Product identifier (e.g., BOLT-M8-50).
     * @param marketCode BCP-47 market code for localized names and descriptions (e.g., pl-PL).
     * @return Localized product catalog entry with name, description, category, specs, and images.
     * @throws com.szelag.pia.exception.ProductNotFoundException  if the product does not exist.
     * @throws com.szelag.pia.exception.CatalogServiceException   if the upstream catalog is unavailable.
     */
    ProductCatalog getProduct(String productId, String marketCode);
}