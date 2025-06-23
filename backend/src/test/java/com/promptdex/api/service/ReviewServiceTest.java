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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PromptRepository promptRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails; // Mock UserDetails for passing to the service

    @InjectMocks
    private ReviewService reviewService;

    private User promptAuthor;
    private User reviewer;
    private Prompt prompt;
    private Review review;
    private CreateReviewRequest createRequest;
    private UpdateReviewRequest updateRequest;

    @BeforeEach
    void setUp() {
        UUID authorId = UUID.randomUUID();
        promptAuthor = new User();
        promptAuthor.setId(authorId);
        promptAuthor.setUsername("promptAuthor");

        UUID reviewerId = UUID.randomUUID();
        reviewer = new User();
        reviewer.setId(reviewerId);
        reviewer.setUsername("reviewer");

        prompt = new Prompt();
        prompt.setId(UUID.randomUUID());
        prompt.setAuthor(promptAuthor);

        review = new Review();
        review.setId(UUID.randomUUID());
        review.setPrompt(prompt);
        review.setUser(reviewer);
        review.setRating(4);
        review.setComment("Good prompt");
        review.setCreatedAt(Instant.now());
        review.setUpdatedAt(Instant.now());

        createRequest = new CreateReviewRequest(5, "Excellent!");
        updateRequest = new UpdateReviewRequest(3, "Okay prompt.");
    }

    // --- FIX 1: Add user repository mocking to all relevant tests ---
    // --- FIX 2: Assert for the correct exception types ---

    @Test
    void createReview_whenUserIsNotAuthorAndHasNotReviewed_shouldSucceed() {
        // GIVEN
        when(userDetails.getUsername()).thenReturn("reviewer");
        when(userRepository.findByUsername("reviewer")).thenReturn(Optional.of(reviewer));
        when(promptRepository.findById(prompt.getId())).thenReturn(Optional.of(prompt));
        when(reviewRepository.existsByPrompt_IdAndUser_Id(prompt.getId(), reviewer.getId())).thenReturn(false);
        when(reviewRepository.saveAndFlush(any(Review.class))).thenReturn(review);

        // WHEN
        ReviewDto result = reviewService.createReview(prompt.getId(), createRequest, userDetails);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.authorUsername()).isEqualTo(reviewer.getUsername());
        verify(reviewRepository).saveAndFlush(any(Review.class));
    }

    @Test
    void createReview_whenUserIsAuthor_shouldThrowAccessDeniedException() {
        // GIVEN
        when(userDetails.getUsername()).thenReturn("promptAuthor");
        when(userRepository.findByUsername("promptAuthor")).thenReturn(Optional.of(promptAuthor));
        when(promptRepository.findById(prompt.getId())).thenReturn(Optional.of(prompt));

        // WHEN & THEN
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.createReview(prompt.getId(), createRequest, userDetails);
        });
    }

    @Test
    void createReview_whenUserHasAlreadyReviewed_shouldThrowReviewAlreadyExistsException() {
        // GIVEN
        when(userDetails.getUsername()).thenReturn("reviewer");
        when(userRepository.findByUsername("reviewer")).thenReturn(Optional.of(reviewer));
        when(promptRepository.findById(prompt.getId())).thenReturn(Optional.of(prompt));
        // Simulate that the review already exists
        when(reviewRepository.existsByPrompt_IdAndUser_Id(prompt.getId(), reviewer.getId())).thenReturn(true);

        // WHEN & THEN
        // This is the correct exception based on the service's logic
        assertThrows(ReviewAlreadyExistsException.class, () -> {
            reviewService.createReview(prompt.getId(), createRequest, userDetails);
        });
    }

    @Test
    void updateReview_whenUserIsAuthorOfReview_shouldSucceed() {
        // GIVEN
        when(userDetails.getUsername()).thenReturn("reviewer");
        when(userRepository.findByUsername("reviewer")).thenReturn(Optional.of(reviewer));
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // WHEN
        ReviewDto result = reviewService.updateReview(review.getId(), updateRequest, userDetails);

        // THEN
        assertThat(result).isNotNull();
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void updateReview_whenUserIsNotAuthorOfReview_shouldThrowAccessDeniedException() {
        // GIVEN
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setUsername("otherUser");

        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUsername("otherUser")).thenReturn(Optional.of(otherUser));
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        // WHEN & THEN
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.updateReview(review.getId(), updateRequest, userDetails);
        });
    }

    @Test
    void deleteReview_whenUserIsAuthorOfReview_shouldSucceed() {
        // GIVEN
        when(userDetails.getUsername()).thenReturn("reviewer");
        when(userRepository.findByUsername("reviewer")).thenReturn(Optional.of(reviewer));
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(any(Review.class));

        // WHEN
        reviewService.deleteReview(review.getId(), userDetails);

        // THEN
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void deleteReview_whenUserIsNotAuthorOfReview_shouldThrowAccessDeniedException() {
        // GIVEN
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setUsername("otherUser");

        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUsername("otherUser")).thenReturn(Optional.of(otherUser));
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        // WHEN & THEN
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.deleteReview(review.getId(), userDetails);
        });
    }
}