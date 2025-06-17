// src/main/java/com/promptdex/api/exception/ResourceNotFoundException.java
package com.promptdex.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for cases where a resource is not found in the database.
 * The @ResponseStatus annotation ensures that if this exception is thrown and not caught
 * by our GlobalExceptionHandler, Spring will automatically return a 404 NOT FOUND status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}