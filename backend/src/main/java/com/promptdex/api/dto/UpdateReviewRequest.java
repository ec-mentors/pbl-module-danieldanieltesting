// src/main/java/com/promptdex/api/dto/UpdateReviewRequest.java
package com.promptdex.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// This record defines the data for updating an existing review.
public record UpdateReviewRequest(
        @NotNull(message = "Rating cannot be null")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,

        @NotBlank(message = "Comment cannot be blank")
        @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
        String comment
) {}