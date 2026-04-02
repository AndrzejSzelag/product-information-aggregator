package com.szelag.pia.model;

import java.time.LocalDate;

/**
 * Snapshot of product availability across warehouses.
 * Includes stock levels and estimated delivery dates for the frontend.
 */
public record ProductAvailability(
        int stockLevel,
        String warehouseLocation,
        LocalDate expectedDelivery,
        boolean inStock
) {}