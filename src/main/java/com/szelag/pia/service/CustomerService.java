package com.szelag.pia.service;

import com.szelag.pia.model.CustomerContext;

/**
 * Provides customer segment and preference data for personalized responses.
 *
 * <p>Optional dependency — called only when a {@code customerId} is present.
 * Unavailability degrades the response gracefully without customer context.
 */
public interface CustomerService {

    /**
     * Returns segment and preference data for the given customer.
     *
     * @param customerId Unique customer identifier.
     * @return Customer context with segment (e.g., PREMIUM) and interest categories.
     */
    CustomerContext getCustomer(String customerId);
}