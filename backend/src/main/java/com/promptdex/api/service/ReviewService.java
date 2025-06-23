package com.promptdex.api.service;

import com.promptdex.api.dto.CreateReviewRequest;
import com.promptdex.api.dto.ReviewDto;
import com.promptdex.api.dto.UpdateReviewRequest;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.exception.ReviewAlreadyExistsException; // Import if not already present
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.Review;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.ReviewRepository;
import com.promptdex.api.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails; // <-- FIX
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private User getUserFromDetails(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + userDetails.getUsername()));
    }

    public ReviewDto createReview(UUID promptId, CreateReviewRequest request, UserDetails currentUser) { // <-- FIX
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));

        User user = getUserFromDetails(currentUser);

        if (prompt.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("You cannot review your own prompt.");
        }

        if (reviewRepository.existsByPrompt_IdAndUser_Id(promptId, user.getId())) {
            throw new ReviewAlreadyExistsException("You have already reviewed this prompt.");
        }

        Review review = new Review();
        review.setPrompt(prompt);
        review.setUser(user);
        review.setRating(request.rating());
        review.setComment(request.comment());

        Review savedReview = reviewRepository.saveAndFlush(review);

        return new ReviewDto(savedReview.getId(), savedReview.getRating(), savedReview.getComment(), savedReview.getUser().getUsername(), savedReview.getCreatedAt(), savedReview.getUpdatedAt());
    }

    public ReviewDto updateReview(UUID reviewId, UpdateReviewRequest request, UserDetails currentUser) { // <-- FIX
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        User user = getUserFromDetails(currentUser);

        if (!review.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to edit this review.");
        }

        review.setRating(request.rating());
        review.setComment(request.comment());

        Review updatedReview = reviewRepository.save(review);

        return new ReviewDto(updatedReview.getId(), updatedReview.getRating(), updatedReview.getComment(), updatedReview.getUser().getUsername(), updatedReview.getCreatedAt(), updatedReview.getUpdatedAt());
    }

    public void deleteReview(UUID reviewId, UserDetails currentUser) { // <-- FIX
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        User user = getUserFromDetails(currentUser);

        if (!review.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this review.");
        }

        reviewRepository.delete(review);
    }
}