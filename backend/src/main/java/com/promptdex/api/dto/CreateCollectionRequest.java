package com.promptdex.api.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record CreateCollectionRequest(
        @NotBlank(message = "Collection name cannot be blank")
        @Size(max = 100, message = "Collection name cannot exceed 100 characters")
        String name,
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description
) {}