// src/main/java/com/promptdex/api/dto/ErrorResponse.java
package com.promptdex.api.dto;

import java.time.Instant;

/**
 * A standardized DTO for returning API error responses.
 *
 * @param statusCode The HTTP status code (e.g., 404, 400).
 * @param message A user-friendly error message.
 * @param timestamp The time the error occurred.
 */
public record ErrorResponse(int statusCode, String message, Instant timestamp) {}