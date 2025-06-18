// src/main/java/com/promptdex/api/dto/PromptDto.java
package com.promptdex.api.dto;

// --- NEW IMPORT ---
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// This is a record, which is immutable. We'll use annotations directly on the components.
public record PromptDto(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("title")
        @NotBlank @Size(max = 255) String title,

        @JsonProperty("text")
        @NotBlank String text,

        @JsonProperty("description")
        @NotBlank String description,

        @JsonProperty("model")
        @NotBlank @Size(max = 100) String model,

        @JsonProperty("category")
        @NotBlank @Size(max = 100) String category,

        @JsonProperty("authorUsername")
        String authorUsername,

        // --- CRITICAL FIX START ---
        // Explicitly name the JSON properties to ensure they are not lost during serialization.
        @JsonProperty("createdAt")
        Instant createdAt,

        @JsonProperty("updatedAt")
        Instant updatedAt,
        // --- CRITICAL FIX END ---

        @JsonProperty("averageRating")
        Double averageRating,

        @JsonProperty("reviews")
        List<ReviewDto> reviews
) {}