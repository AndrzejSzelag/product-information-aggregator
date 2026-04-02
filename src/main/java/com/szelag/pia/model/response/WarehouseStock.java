package com.szelag.pia.model.response;

import java.time.LocalDate;

public record WarehouseStock(
        String warehouseLocation,
        int stockLevel,
        LocalDate expectedDelivery) {
}