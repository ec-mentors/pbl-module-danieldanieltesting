package com.promptdex.api.mapper;

import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.dto.ReviewDto;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.Tag;
import com.promptdex.api.model.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptMapper {

    public PromptDto toDto(Prompt prompt, User user) {
        if (prompt == null) {
            return null;
        }

        boolean isBookmarked = user != null && user.getBookmarkedPrompts().stream()
                .anyMatch(bookmarkedPrompt -> bookmarkedPrompt.getId().equals(prompt.getId()));

        List<String> tagNames = prompt.getTags() != null
                ? prompt.getTags().stream().map(Tag::getName).collect(Collectors.toList())
                : Collections.emptyList();

        List<ReviewDto> reviewDtos = prompt.getReviews() != null
                ? prompt.getReviews().stream().map(review -> new ReviewDto(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUser().getUsername(),
                review.getCreatedAt(), // No conversion needed
                review.getUpdatedAt()  // No conversion needed
        )).collect(Collectors.toList())
                : List.of();

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
                prompt.getCreatedAt(), // No conversion needed
                prompt.getUpdatedAt(), // No conversion needed
                averageRating,
                reviewDtos,
                tagNames,
                isBookmarked
        );
    }
}