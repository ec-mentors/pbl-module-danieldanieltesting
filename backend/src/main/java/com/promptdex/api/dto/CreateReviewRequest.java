package com.promptdex.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull @Min(1) @Max(5)
        int rating,
        @Size(max = 5000, message = "Comment must not exceed 5000 characters")
        String comment
) {
}