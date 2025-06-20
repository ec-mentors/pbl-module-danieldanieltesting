// src/main/java/com/promptdex/api/service/ReviewService.java
package com.promptdex.api.service;

import com.promptdex.api.dto.CreateReviewRequest;
import com.promptdex.api.dto.ReviewDto;
import com.promptdex.api.dto.UpdateReviewRequest;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.exception.ReviewAlreadyExistsException;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.Review;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.ReviewRepository;
import com.promptdex.api.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.UUID;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PromptRepository promptRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, PromptRepository promptRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.promptRepository = promptRepository;
        this.userRepository = userRepository;
    }

    public ReviewDto addReviewToPrompt(UUID promptId, CreateReviewRequest reviewRequest, String username) {
        if (reviewRepository.existsByPrompt_IdAndUser_Username(promptId, username)) {
            throw new ReviewAlreadyExistsException("User has already submitted a review for this prompt.");
        }

        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Review review = new Review();
        review.setPrompt(prompt);
        review.setUser(user);
        review.setRating(reviewRequest.rating());
        review.setComment(reviewRequest.comment());

        // --- THIS IS THE FIX ---
        // Use saveAndFlush() to immediately write to the DB and populate the @CreationTimestamp
        // and @UpdateTimestamp fields on the returned object.
        Review savedReview = reviewRepository.saveAndFlush(review);

        return new ReviewDto(
                savedReview.getId(),
                savedReview.getRating(),
                savedReview.getComment(),
                savedReview.getUser().getUsername(),
                savedReview.getCreatedAt().toInstant(ZoneOffset.UTC),
                savedReview.getUpdatedAt().toInstant(ZoneOffset.UTC)
        );
    }

    public ReviewDto updateReview(UUID reviewId, UpdateReviewRequest reviewRequest, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("User is not authorized to update this review.");
        }

        review.setRating(reviewRequest.rating());
        review.setComment(reviewRequest.comment());

        // Using save() here is fine because the timestamps are already populated.
        // However, for consistency, saveAndFlush() is also perfectly safe.
        Review updatedReview = reviewRepository.save(review);

        return new ReviewDto(
                updatedReview.getId(),
                updatedReview.getRating(),
                updatedReview.getComment(),
                updatedReview.getUser().getUsername(),
                updatedReview.getCreatedAt().toInstant(ZoneOffset.UTC),
                updatedReview.getUpdatedAt().toInstant(ZoneOffset.UTC)
        );
    }

    public void deleteReview(UUID reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("User is not authorized to delete this review.");
        }

        reviewRepository.delete(review);
    }
}