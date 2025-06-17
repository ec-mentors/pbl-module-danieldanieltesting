// src/main/java/com/promptdex/api/service/PromptService.java
package com.promptdex.api.service;

import com.promptdex.api.dto.CreatePromptRequest;
import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.dto.ReviewDto;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.Review;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PromptService {

    private final PromptRepository promptRepository;
    private final UserRepository userRepository;

    public PromptService(PromptRepository promptRepository, UserRepository userRepository) {
        this.promptRepository = promptRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PromptDto> getAllPrompts() {
        return promptRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromptDto getPromptById(UUID promptId) {
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));
        return convertToDto(prompt);
    }

    @Transactional
    public PromptDto createPrompt(CreatePromptRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Prompt prompt = new Prompt();
        prompt.setTitle(request.title());
        prompt.setPromptText(request.text());
        prompt.setDescription(request.description());
        prompt.setTargetAiModel(request.model());
        prompt.setCategory(request.category());
        prompt.setAuthor(user);

        // --- THE FIX IS HERE ---
        // Use saveAndFlush to force the SQL INSERT and immediately populate
        // the @CreationTimestamp and @UpdateTimestamp fields on the entity instance.
        Prompt savedPrompt = promptRepository.saveAndFlush(prompt);

        // Now, when we call convertToDto, savedPrompt is guaranteed to have non-null timestamps.
        return convertToDto(savedPrompt);
    }

    @Transactional
    public PromptDto updatePrompt(UUID promptId, CreatePromptRequest request, String username) throws AccessDeniedException {
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));

        if (!prompt.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to edit this prompt.");
        }

        prompt.setTitle(request.title());
        prompt.setPromptText(request.text());
        prompt.setDescription(request.description());
        prompt.setTargetAiModel(request.model());
        prompt.setCategory(request.category());

        Prompt updatedPrompt = promptRepository.save(prompt);
        return convertToDto(updatedPrompt);
    }

    @Transactional
    public void deletePrompt(UUID promptId, String username) throws AccessDeniedException {
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));

        if (!prompt.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to delete this prompt.");
        }

        promptRepository.delete(prompt);
    }

    private PromptDto convertToDto(Prompt prompt) {
        List<Review> reviews = prompt.getReviews();

        Double averageRating = (reviews != null && !reviews.isEmpty())
                ? reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0)
                : 0.0;

        List<ReviewDto> reviewDtos = (reviews != null)
                ? reviews.stream()
                .map(review -> new ReviewDto(
                        review.getId(),
                        review.getRating(),
                        review.getComment(),
                        review.getUser().getUsername(),
                        review.getCreatedAt().toInstant(ZoneOffset.UTC)
                ))
                .collect(Collectors.toList())
                : Collections.emptyList();

        return new PromptDto(
                prompt.getId(),
                prompt.getTitle(),
                prompt.getPromptText(),
                prompt.getDescription(),
                prompt.getTargetAiModel(),
                prompt.getCategory(),
                prompt.getAuthor().getUsername(),
                prompt.getCreatedAt().toInstant(ZoneOffset.UTC),
                prompt.getUpdatedAt().toInstant(ZoneOffset.UTC),
                averageRating,
                reviewDtos
        );
    }
}