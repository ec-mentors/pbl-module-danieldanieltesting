package com.promptdex.api.service;

import com.promptdex.api.dto.CreateReviewRequest;
import com.promptdex.api.dto.ReviewDto;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.Review;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.ReviewRepository;
import com.promptdex.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;     // <-- Import Instant
import java.time.ZoneOffset;  // <-- Import ZoneOffset for conversion

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PromptRepository promptRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, PromptRepository promptRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.promptRepository = promptRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReviewDto createReview(CreateReviewRequest createReviewRequest, String username) {
        // 1. Find the parent entities
        Prompt prompt = promptRepository.findById(createReviewRequest.promptId())
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + createReviewRequest.promptId()));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        // 2. Create the new Review entity
        Review review = new Review();
        review.setRating(createReviewRequest.rating());
        review.setComment(createReviewRequest.comment());
        review.setUser(user);
        review.setPrompt(prompt);

        // 3. Save the entity. Hibernate will now use @CreationTimestamp
        // to set the `createdAt` field before the INSERT statement.
        Review savedReview = reviewRepository.save(review);

        // 4. Convert the saved entity to our output DTO
        return convertToDto(savedReview);
    }

    // --- Corrected Helper Method ---
    private ReviewDto convertToDto(Review review) {
        // This is the definitive fix for the NullPointerException and the type mismatch.
        // It safely handles the conversion from the entity's LocalDateTime to the DTO's Instant.
        Instant createdAtInstant = (review.getCreatedAt() != null)
                ? review.getCreatedAt().toInstant(ZoneOffset.UTC)
                : Instant.now(); // Fallback to now() just in case, though it shouldn't be needed

        return new ReviewDto(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUser().getUsername(),
                createdAtInstant // Pass the correctly typed and non-null Instant
        );
    }
}