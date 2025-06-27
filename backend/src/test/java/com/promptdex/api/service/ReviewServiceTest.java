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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private UserDetails userDetails;
    @InjectMocks
    private ReviewService reviewService;
    private User promptAuthor;
    private User reviewer;
    private Prompt prompt;
    private Review review;
    private CreateReviewRequest createRequest;
    private UpdateReviewRequest updateRequest;
    private UUID promptId;
    private UUID reviewId;

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
        promptId = UUID.randomUUID();
        prompt = new Prompt();
        prompt.setId(promptId);
        prompt.setAuthor(promptAuthor);
        reviewId = UUID.randomUUID();
        review = new Review();
        review.setId(reviewId);
        review.setPrompt(prompt);
        review.setUser(reviewer);
        review.setRating(4);
        review.setComment("Good prompt");
        review.setCreatedAt(Instant.now());
        review.setUpdatedAt(Instant.now());
        createRequest = new CreateReviewRequest(5, "Excellent!");
        updateRequest = new UpdateReviewRequest(3, "Okay prompt.");
    }

    private void mockUpdatePromptAverageRatingInteractions(UUID targetPromptId, Double newAverageRating) {
        when(promptRepository.findById(targetPromptId)).thenReturn(Optional.of(prompt));
        when(promptRepository.findAverageRatingByPromptId(targetPromptId)).thenReturn(Optional.ofNullable(newAverageRating));
        when(promptRepository.save(any(Prompt.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createReview_whenUserIsNotAuthorAndHasNotReviewed_shouldSucceed() {
        when(userDetails.getUsername()).thenReturn(reviewer.getUsername());
        when(userRepository.findByUsername(reviewer.getUsername())).thenReturn(Optional.of(reviewer));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));
        when(reviewRepository.existsByPrompt_IdAndUser_Id(promptId, reviewer.getId())).thenReturn(false);
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        Review savedReview = new Review();
        savedReview.setId(UUID.randomUUID());
        savedReview.setPrompt(prompt);
        savedReview.setUser(reviewer);
        savedReview.setRating(createRequest.rating());
        savedReview.setComment(createRequest.comment());
        savedReview.setCreatedAt(Instant.now());
        savedReview.setUpdatedAt(Instant.now());
        when(reviewRepository.saveAndFlush(reviewCaptor.capture())).thenReturn(savedReview);
        mockUpdatePromptAverageRatingInteractions(promptId, (double) createRequest.rating());
        ReviewDto result = reviewService.createReview(promptId, createRequest, userDetails);
        assertThat(result).isNotNull();
        assertThat(result.authorUsername()).isEqualTo(reviewer.getUsername());
        assertThat(result.rating()).isEqualTo(createRequest.rating());
        assertThat(result.comment()).isEqualTo(createRequest.comment());
        Review capturedReview = reviewCaptor.getValue();
        assertThat(capturedReview.getPrompt()).isEqualTo(prompt);
        assertThat(capturedReview.getUser()).isEqualTo(reviewer);
        assertThat(capturedReview.getRating()).isEqualTo(createRequest.rating());
        verify(reviewRepository).saveAndFlush(any(Review.class));
        verify(promptRepository, times(2)).findById(promptId);
        verify(promptRepository).findAverageRatingByPromptId(promptId);
        verify(promptRepository).save(prompt);
    }

    @Test
    void createReview_whenPromptNotFound_shouldThrowResourceNotFoundException() {
        when(promptRepository.findById(promptId)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.createReview(promptId, createRequest, userDetails);
        });
        assertThat(exception.getMessage()).contains("Prompt not found with id: " + promptId);
        verify(reviewRepository, never()).saveAndFlush(any());
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void createReview_whenUserIsAuthor_shouldThrowAccessDeniedException() {
        when(userDetails.getUsername()).thenReturn(promptAuthor.getUsername());
        when(userRepository.findByUsername(promptAuthor.getUsername())).thenReturn(Optional.of(promptAuthor));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            reviewService.createReview(promptId, createRequest, userDetails);
        });
        assertEquals("You cannot review your own prompt.", exception.getMessage());
        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void createReview_whenUserHasAlreadyReviewed_shouldThrowReviewAlreadyExistsException() {
        when(userDetails.getUsername()).thenReturn(reviewer.getUsername());
        when(userRepository.findByUsername(reviewer.getUsername())).thenReturn(Optional.of(reviewer));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));
        when(reviewRepository.existsByPrompt_IdAndUser_Id(promptId, reviewer.getId())).thenReturn(true);
        ReviewAlreadyExistsException exception = assertThrows(ReviewAlreadyExistsException.class, () -> {
            reviewService.createReview(promptId, createRequest, userDetails);
        });
        assertEquals("You have already reviewed this prompt.", exception.getMessage());
        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateReview_whenUserIsAuthorOfReview_shouldSucceed() {
        when(userDetails.getUsername()).thenReturn(reviewer.getUsername());
        when(userRepository.findByUsername(reviewer.getUsername())).thenReturn(Optional.of(reviewer));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        when(reviewRepository.save(reviewCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        mockUpdatePromptAverageRatingInteractions(promptId, (double) updateRequest.rating());
        ReviewDto result = reviewService.updateReview(reviewId, updateRequest, userDetails);
        assertThat(result).isNotNull();
        assertThat(result.rating()).isEqualTo(updateRequest.rating());
        assertThat(result.comment()).isEqualTo(updateRequest.comment());
        assertThat(result.authorUsername()).isEqualTo(reviewer.getUsername());
        Review captured = reviewCaptor.getValue();
        assertThat(captured.getId()).isEqualTo(reviewId);
        assertThat(captured.getRating()).isEqualTo(updateRequest.rating());
        assertThat(captured.getComment()).isEqualTo(updateRequest.comment());
        verify(reviewRepository).save(any(Review.class));
        verify(promptRepository).findById(promptId);
        verify(promptRepository).findAverageRatingByPromptId(promptId);
        verify(promptRepository).save(prompt);
    }

    @Test
    void updateReview_whenReviewNotFound_shouldThrowResourceNotFoundException() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.updateReview(reviewId, updateRequest, userDetails);
        });
        assertThat(exception.getMessage()).contains("Review not found with id: " + reviewId);
        verify(reviewRepository, never()).save(any());
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void updateReview_whenUserIsNotAuthorOfReview_shouldThrowAccessDeniedException() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setUsername("otherUser");
        when(userDetails.getUsername()).thenReturn(otherUser.getUsername());
        when(userRepository.findByUsername(otherUser.getUsername())).thenReturn(Optional.of(otherUser));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            reviewService.updateReview(reviewId, updateRequest, userDetails);
        });
        assertEquals("You do not have permission to edit this review.", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteReview_whenUserIsAuthorOfReview_shouldSucceed() {
        when(userDetails.getUsername()).thenReturn(reviewer.getUsername());
        when(userRepository.findByUsername(reviewer.getUsername())).thenReturn(Optional.of(reviewer));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(eq(review));
        mockUpdatePromptAverageRatingInteractions(promptId, null);
        reviewService.deleteReview(reviewId, userDetails);
        verify(reviewRepository, times(1)).delete(review);
        verify(promptRepository).findById(promptId);
        verify(promptRepository).findAverageRatingByPromptId(promptId);
        verify(promptRepository).save(prompt);
    }

    @Test
    void deleteReview_whenReviewNotFound_shouldThrowResourceNotFoundException() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.deleteReview(reviewId, userDetails);
        });
        assertThat(exception.getMessage()).contains("Review not found with id: " + reviewId);
        verify(reviewRepository, never()).delete(any());
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void deleteReview_whenUserIsNotAuthorOfReview_shouldThrowAccessDeniedException() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setUsername("otherUser");
        when(userDetails.getUsername()).thenReturn(otherUser.getUsername());
        when(userRepository.findByUsername(otherUser.getUsername())).thenReturn(Optional.of(otherUser));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            reviewService.deleteReview(reviewId, userDetails);
        });
        assertEquals("You do not have permission to delete this review.", exception.getMessage());
        verify(reviewRepository, never()).delete(any());
    }
}