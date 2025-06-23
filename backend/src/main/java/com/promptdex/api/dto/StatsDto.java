package com.promptdex.api.dto;

public record StatsDto(
        long totalUsers,
        long totalPrompts,
        long totalReviews
        // Add more stats here in the future if needed
) {
}