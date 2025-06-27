package com.promptdex.api.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record CreatePromptRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String text,
        @NotBlank String description,
        @NotBlank @Size(max = 100) String model,
        @NotBlank @Size(max = 100) String category
) {}