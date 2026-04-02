package com.szelag.pia.model;

import java.util.List;

/**
 * Domain model for customer-specific context.
 * Used for personalized pricing and segment-based logic.
 */
public record CustomerContext(
    String customerId,
    String segment,
    List<String> preferences,
    List<String> purchaseHistory
) {}