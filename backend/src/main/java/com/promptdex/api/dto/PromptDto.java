package com.promptdex.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PromptDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("title") String title,
        @JsonProperty("text") String text,
        @JsonProperty("description") String description,
        @JsonProperty("model") String model,
        @JsonProperty("category") String category,
        @JsonProperty("authorUsername") String authorUsername,
        @JsonProperty("createdAt") Instant createdAt,
        @JsonProperty("updatedAt") Instant updatedAt,
        @JsonProperty("averageRating") double averageRating, // Use primitive double
        @JsonProperty("reviews") List<ReviewDto> reviews,
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("isBookmarked") boolean isBookmarked
) {}