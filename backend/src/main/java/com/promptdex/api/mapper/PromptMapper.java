// src/main/java/com/promptdex/api/mapper/PromptMapper.java
package com.promptdex.api.mapper;

import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.dto.ReviewDto;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.Tag;
import com.promptdex.api.model.User;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptMapper {

    /**
     * Maps a Prompt entity to a PromptDto.
     *
     * @param prompt The Prompt entity to map.
     * @param user   The currently authenticated user, used to determine if the prompt is bookmarked. Can be null for anonymous users.
     * @return The resulting PromptDto.
     */
    public PromptDto toDto(Prompt prompt, User user) {
        if (prompt == null) {
            return null;
        }

        boolean isBookmarked = user != null && user.getBookmarkedPrompts().stream()
                .anyMatch(bookmarkedPrompt -> bookmarkedPrompt.getId().equals(prompt.getId()));

        List<String> tagNames = prompt.getTags() != null
                ? prompt.getTags().stream().map(Tag::getName).collect(Collectors.toList())
                : Collections.emptyList();

        // --- FIX: Use List.of() for better type inference to avoid conditional expression error ---
        List<ReviewDto> reviewDtos = prompt.getReviews() != null
                ? prompt.getReviews().stream().map(review -> new ReviewDto(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUser().getUsername(),
                // --- FIX: Add required ZoneOffset.UTC for toInstant() conversion ---
                review.getCreatedAt().toInstant(ZoneOffset.UTC),
                review.getUpdatedAt().toInstant(ZoneOffset.UTC)
        )).collect(Collectors.toList()) : List.of(); // <-- CORRECTED

        double averageRating = prompt.getReviews() != null && !prompt.getReviews().isEmpty()
                ? prompt.getReviews().stream().mapToInt(r -> r.getRating()).average().orElse(0.0)
                : 0.0;


        return new PromptDto(
                prompt.getId(),
                prompt.getTitle(),
                prompt.getPromptText(),
                prompt.getDescription(),
                prompt.getTargetAiModel(),
                prompt.getCategory(),
                prompt.getAuthor().getUsername(),
                // --- FIX: Add required ZoneOffset.UTC for toInstant() conversion ---
                prompt.getCreatedAt().toInstant(ZoneOffset.UTC),
                prompt.getUpdatedAt().toInstant(ZoneOffset.UTC),
                averageRating,
                reviewDtos,
                tagNames,
                isBookmarked
        );
    }
}