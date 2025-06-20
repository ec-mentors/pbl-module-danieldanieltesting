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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    private Set<UUID> getBookmarkedPromptIds(UserDetails userDetails) {
        if (userDetails == null) {
            return Collections.emptySet();
        }
        User user = userRepository.findByUsernameWithBookmarks(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return Collections.emptySet();
        }
        return user.getBookmarkedPrompts().stream()
                .map(Prompt::getId)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Page<PromptDto> searchAndPagePrompts(String searchTerm, int page, int size, UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Prompt> promptPage = promptRepository.searchAndPagePrompts(searchTerm, pageable);
        Set<UUID> bookmarkedIds = getBookmarkedPromptIds(userDetails);
        return promptPage.map(prompt -> convertToDto(prompt, bookmarkedIds));
    }

    @Transactional(readOnly = true)
    public Page<PromptDto> getPromptsByAuthorUsername(String username, int page, int size, UserDetails userDetails) {
        userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Prompt> promptPage = promptRepository.findByAuthor_Username(username, pageable);
        Set<UUID> bookmarkedIds = getBookmarkedPromptIds(userDetails);
        return promptPage.map(prompt -> convertToDto(prompt, bookmarkedIds));
    }

    @Transactional(readOnly = true)
    public PromptDto getPromptById(UUID promptId, UserDetails userDetails) {
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));
        Set<UUID> bookmarkedIds = getBookmarkedPromptIds(userDetails);
        return convertToDto(prompt, bookmarkedIds);
    }

    @Transactional
    public void addBookmark(UUID promptId, String username) {
        User user = userRepository.findByUsernameWithBookmarks(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));
        user.getBookmarkedPrompts().add(prompt);
    }

    @Transactional
    public void removeBookmark(UUID promptId, String username) {
        User user = userRepository.findByUsernameWithBookmarks(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));
        user.getBookmarkedPrompts().remove(prompt);
    }

    @Transactional(readOnly = true)
    public Page<PromptDto> getBookmarkedPrompts(String username, int page, int size) {
        userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        // --- THE FIX ---
        // Removed the "p." alias. Sort should use the entity property name directly.
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Prompt> promptPage = promptRepository.findByBookmarkedByUsers_Username(username, pageable);
        Set<UUID> bookmarkedIds = promptPage.getContent().stream().map(Prompt::getId).collect(Collectors.toSet());
        return promptPage.map(prompt -> convertToDto(prompt, bookmarkedIds));
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
        Prompt savedPrompt = promptRepository.saveAndFlush(prompt);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (auth != null && auth.getPrincipal() instanceof UserDetails) ? (UserDetails) auth.getPrincipal() : null;
        return convertToDto(savedPrompt, getBookmarkedPromptIds(userDetails));
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (auth != null && auth.getPrincipal() instanceof UserDetails) ? (UserDetails) auth.getPrincipal() : null;
        return convertToDto(updatedPrompt, getBookmarkedPromptIds(userDetails));
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

    private PromptDto convertToDto(Prompt prompt, Set<UUID> bookmarkedPromptIds) {
        Instant createdAtInstant = prompt.getCreatedAt() != null ? prompt.getCreatedAt().toInstant(ZoneOffset.UTC) : null;
        Instant updatedAtInstant = prompt.getUpdatedAt() != null ? prompt.getUpdatedAt().toInstant(ZoneOffset.UTC) : null;
        List<Review> reviews = prompt.getReviews();
        Double averageRating = (reviews != null && !reviews.isEmpty()) ? reviews.stream().mapToInt(Review::getRating).average().orElse(0.0) : 0.0;
        List<ReviewDto> reviewDtos = (reviews != null) ? reviews.stream().map(review -> new ReviewDto(review.getId(), review.getRating(), review.getComment(), review.getUser().getUsername(), (review.getCreatedAt() != null ? review.getCreatedAt().toInstant(ZoneOffset.UTC) : null))).collect(Collectors.toList()) : Collections.emptyList();

        return new PromptDto(
                prompt.getId(),
                prompt.getTitle(),
                prompt.getPromptText(),
                prompt.getDescription(),
                prompt.getTargetAiModel(),
                prompt.getCategory(),
                prompt.getAuthor().getUsername(),
                createdAtInstant,
                updatedAtInstant,
                averageRating,
                reviewDtos,
                bookmarkedPromptIds.contains(prompt.getId())
        );
    }
}