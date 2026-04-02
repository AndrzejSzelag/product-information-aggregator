package com.szelag.pia.model.response;

import java.time.Instant;

import lombok.Builder;

/**
 * Standardized error response model. Uses Java Record for immutability and
 * @Builder for clear assembly in ExceptionHandlers.
 */
@Builder
public record ErrorResponse(
        String error,
        String message,
        int status,
        Instant timestamp) {

}
