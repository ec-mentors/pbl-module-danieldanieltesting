package com.promptdex.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

// This record defines the data required to CREATE a review.
// Its field names MUST match the JSON sent by the frontend form.
public record CreateReviewRequest(
        @NotNull(message = "Rating cannot be null")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,

        @NotBlank(message = "Comment cannot be blank")
        String comment,

        @NotNull(message = "Prompt ID cannot be null")
        UUID promptId
) {}