package com.promptdex.api.dto;

import java.time.Instant;

public record ErrorResponse(int statusCode, String message, Instant timestamp) {
}