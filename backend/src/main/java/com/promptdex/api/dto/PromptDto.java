package com.promptdex.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

        @JsonProperty("createdAt")
        Instant createdAt,

        @JsonProperty("updatedAt")
        Instant updatedAt,

        @JsonProperty("averageRating")
        Double averageRating,

        @JsonProperty("reviews")
        List<ReviewDto> reviews,

        // --- NEW FIELD ---
        @JsonProperty("isBookmarked")
        boolean isBookmarked
) {}