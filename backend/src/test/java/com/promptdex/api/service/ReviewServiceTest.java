package com.promptdex.api.service;

import com.promptdex.api.dto.CreateReviewRequest;
import com.promptdex.api.dto.UpdateReviewRequest;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.Review;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.ReviewRepository;
import com.promptdex.api.repository.UserRepository;
import com.promptdex.api.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private PromptRepository promptRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User promptAuthor;
    private User reviewer;
    private Prompt prompt;
    private Review review;
    private UserPrincipal reviewerPrincipal;

    @BeforeEach
    void setUp() {
        promptAuthor = new User();
        promptAuthor.setId(UUID.randomUUID());
        promptAuthor.setUsername("promptAuthor");

        reviewer = new User();
        reviewer.setId(UUID.randomUUID());
        reviewer.setUsername("reviewer");
        reviewerPrincipal = new UserPrincipal(reviewer);

        prompt = new Prompt();
        prompt.setId(UUID.randomUUID());
        prompt.setAuthor(promptAuthor);

        review = new Review();
        review.setId(UUID.randomUUID());
        review.setUser(reviewer);
        review.setPrompt(prompt);
        review.setRating(5);
    }

    @Test
    void createReview_whenUserIsNotAuthorAndHasNotReviewed_shouldSucceed() {
        // Arrange
        CreateReviewRequest request = new CreateReviewRequest(4, "Great prompt!");
        when(promptRepository.findById(prompt.getId())).thenReturn(Optional.of(prompt));
        when(userRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(reviewRepository.existsByPrompt_IdAndUser_Username(prompt.getId(), "reviewer")).thenReturn(false);
        when(reviewRepository.saveAndFlush(any(Review.class))).thenReturn(review);

        // Act
        reviewService.createReview(prompt.getId(), request, reviewerPrincipal);

        // Assert
        verify(reviewRepository, times(1)).saveAndFlush(any(Review.class));
    }

    @Test
    void createReview_whenUserIsAuthor_shouldThrowAccessDeniedException() {
        // Arrange
        CreateReviewRequest request = new CreateReviewRequest(4, "Test");
        UserPrincipal authorPrincipal = new UserPrincipal(promptAuthor);
        prompt.setAuthor(promptAuthor); // Explicitly set

        when(promptRepository.findById(prompt.getId())).thenReturn(Optional.of(prompt));
        when(userRepository.findById(promptAuthor.getId())).thenReturn(Optional.of(promptAuthor));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.createReview(prompt.getId(), request, authorPrincipal);
        });

        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void createReview_whenUserHasAlreadyReviewed_shouldThrowAccessDeniedException() {
        // Arrange
        CreateReviewRequest request = new CreateReviewRequest(4, "Test");
        when(promptRepository.findById(prompt.getId())).thenReturn(Optional.of(prompt));
        when(userRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        // Simulate that a review already exists
        when(reviewRepository.existsByPrompt_IdAndUser_Username(prompt.getId(), "reviewer")).thenReturn(true);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.createReview(prompt.getId(), request, reviewerPrincipal);
        });
    }

    @Test
    void updateReview_whenUserIsAuthorOfReview_shouldSucceed() {
        // Arrange
        UpdateReviewRequest request = new UpdateReviewRequest(3, "Updated comment");
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // Act
        reviewService.updateReview(review.getId(), request, reviewerPrincipal);

        // Assert
        verify(reviewRepository).save(review);
        assertEquals(3, review.getRating());
        assertEquals("Updated comment", review.getComment());
    }

    @Test
    void updateReview_whenUserIsNotAuthorOfReview_shouldThrowAccessDeniedException() {
        // Arrange
        UpdateReviewRequest request = new UpdateReviewRequest(3, "Updated comment");
        // Create a different user trying to edit the review
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        UserPrincipal anotherUserPrincipal = new UserPrincipal(anotherUser);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.updateReview(review.getId(), request, anotherUserPrincipal);
        });
    }

    @Test
    void deleteReview_whenUserIsAuthorOfReview_shouldSucceed() {
        // Arrange
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        // Act
        reviewService.deleteReview(review.getId(), reviewerPrincipal);

        // Assert
        verify(reviewRepository, times(1)).delete(review);

    }

    @Test
    void deleteReview_whenUserIsNotAuthorOfReview_shouldThrowAccessDeniedException() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        UserPrincipal anotherUserPrincipal = new UserPrincipal(anotherUser);
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.deleteReview(review.getId(), anotherUserPrincipal);
        });

        verify(reviewRepository, never()).delete(any());
    }
}