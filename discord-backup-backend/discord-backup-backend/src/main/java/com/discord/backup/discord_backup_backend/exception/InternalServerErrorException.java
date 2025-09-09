package com.discord.backup.discord_backup_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to represent an internal server error (HTTP 500).
 * This can be thrown when an unexpected condition prevents the server
 * from fulfilling the request.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Sets the default HTTP status code
public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(String message) {
        super(message);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalServerErrorException(Throwable cause) {
        super(cause);
    }
}