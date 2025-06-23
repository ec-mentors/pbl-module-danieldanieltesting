package com.promptdex.api.service;

import com.promptdex.api.dto.CreateReviewRequest;
import com.promptdex.api.dto.ReviewDto;
import com.promptdex.api.dto.UpdateReviewRequest;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.Review;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.ReviewRepository;
import com.promptdex.api.repository.UserRepository;
import com.promptdex.api.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
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

    // The controller now correctly calls this method
    public ReviewDto createReview(UUID promptId, CreateReviewRequest request, UserPrincipal currentUser) {
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        if (prompt.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("You cannot review your own prompt.");
        }

        if (reviewRepository.existsByPrompt_IdAndUser_Username(promptId, currentUser.getUsername())) {
            throw new AccessDeniedException("You have already reviewed this prompt.");
        }

        Review review = new Review();
        review.setPrompt(prompt);
        review.setUser(user);
        review.setRating(request.rating());
        review.setComment(request.comment());

        Review savedReview = reviewRepository.saveAndFlush(review);

        return new ReviewDto(
                savedReview.getId(),
                savedReview.getRating(),
                savedReview.getComment(),
                savedReview.getUser().getUsername(),
                savedReview.getCreatedAt(),
                savedReview.getUpdatedAt()
        );
    }

    public ReviewDto updateReview(UUID reviewId, UpdateReviewRequest request, UserPrincipal currentUser) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to edit this review.");
        }

        review.setRating(request.rating());
        review.setComment(request.comment());

        Review updatedReview = reviewRepository.save(review);

        return new ReviewDto(
                updatedReview.getId(),
                updatedReview.getRating(),
                updatedReview.getComment(),
                updatedReview.getUser().getUsername(),
                updatedReview.getCreatedAt(),
                updatedReview.getUpdatedAt()
        );
    }

    public void deleteReview(UUID reviewId, UserPrincipal currentUser) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this review.");
        }

        reviewRepository.delete(review);
    }
}