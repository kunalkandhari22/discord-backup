package com.discord.backup.discord_backup_backend.error;

import com.discord.backup.discord_backup_backend.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for the reactive application.
 * Catches exceptions thrown by controllers and provides structured error responses.
 */
@ControllerAdvice
@Order(-100) // Lower order to ensure it runs before default handlers
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles custom InternalServerErrorException.
     * Returns HTTP 500 status with a structured error response.
     *
     * @param ex      The InternalServerErrorException that was thrown.
     * @param exchange The current reactive web exchange.
     * @return A Mono of ResponseEntity containing the ErrorResponse and HTTP 500 status.
     */
    @ExceptionHandler(InternalServerErrorException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInternalServerErrorException(
            InternalServerErrorException ex, ServerWebExchange exchange) {

        logger.error("Internal Server Error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(), // User-friendly message
                exchange.getRequest().getURI().getPath()
        );
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * General exception handler for any unhandled exceptions.
     * This acts as a fallback to catch any exceptions not specifically handled
     * by other @ExceptionHandler methods.
     * Returns HTTP 500 status with a generic error message.
     *
     * @param ex      The general Exception that was thrown.
     * @param exchange The current reactive web exchange.
     * @return A Mono of ResponseEntity containing the ErrorResponse and HTTP 500 status.
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {

        // Log the full exception for internal debugging
        logger.error("Unhandled Exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", // Generic, safe message
                exchange.getRequest().getURI().getPath()
        );
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
