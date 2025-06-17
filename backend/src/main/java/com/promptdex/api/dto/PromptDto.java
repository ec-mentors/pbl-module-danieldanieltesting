// src/main/java/com/promptdex/api/dto/PromptDto.java
package com.promptdex.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant; // Keep this for now, we'll fix timestamps later
import java.util.List;
import java.util.UUID; // <-- Import UUID

public record PromptDto(
        UUID id, // <-- CHANGE THIS FROM Long to UUID
        @NotBlank @Size(max = 255) String title,
        @NotBlank String text,
        @NotBlank String description,
        @NotBlank @Size(max = 100) String model,
        @NotBlank @Size(max = 100) String category,
        String authorUsername,
        Instant createdAt,
        Instant updatedAt,
        Double averageRating,
        List<ReviewDto> reviews
) {}