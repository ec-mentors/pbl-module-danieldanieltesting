package com.promptdex.api.dto;
import java.time.Instant;
import java.util.UUID;
public record ReviewDto(
        UUID id,
        int rating,
        String comment,
        String authorUsername,
        Instant createdAt,
        Instant updatedAt
) {}