package com.szelag.pia.exception;

/**
 * Thrown when the mandatory Catalog Service fails.
 * This is a "hard dependency" — aggregation cannot continue without it.
 */
public class CatalogServiceException extends RuntimeException {
    public CatalogServiceException(String message) {
        super(message);
    }

    public CatalogServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}