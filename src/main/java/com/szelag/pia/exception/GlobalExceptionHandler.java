package com.szelag.pia.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.szelag.pia.model.response.ErrorResponse;

/**
 * Global interceptor for all exceptions across the API.
 * Ensures consistent, structured error responses for the client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ProductNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.builder()
                                                .error("PRODUCT_NOT_FOUND")
                                                .message(ex.getMessage())
                                                .status(404)
                                                .timestamp(Instant.now())
                                                .build());
        }

        /**
         * Handles mandatory Catalog Service failures as a Service Unavailable error.
         */
        @ExceptionHandler(CatalogServiceException.class)
        public ResponseEntity<ErrorResponse> handleCatalogFailure(CatalogServiceException ex) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(ErrorResponse.builder()
                                                .error("CATALOG_SERVICE_UNAVAILABLE")
                                                .message("Product information is currently unavailable. Please try again later.")
                                                .status(503)
                                                .timestamp(Instant.now())
                                                .build());
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.builder()
                                                .error("MISSING_PARAMETER")
                                                .message(ex.getMessage())
                                                .status(400)
                                                .timestamp(Instant.now())
                                                .build());
        }

        /**
         * Fallback for unexpected internal errors.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.builder()
                                                .error("INTERNAL_ERROR")
                                                .message("An unexpected error occurred.")
                                                .status(500)
                                                .timestamp(Instant.now())
                                                .build());
        }
}