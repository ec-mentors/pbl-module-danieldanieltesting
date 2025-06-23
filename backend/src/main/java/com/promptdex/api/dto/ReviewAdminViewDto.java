package com.promptdex.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewAdminViewDto(
        UUID id,
        int rating,
        String comment,
        String authorUsername,
        UUID promptId,
        String promptTitle,
        Instant createdAt,
        Instant updatedAt
) {}