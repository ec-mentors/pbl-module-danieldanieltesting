package com.promptdex.api.controller;

import com.promptdex.api.dto.CreateReviewRequest;
import com.promptdex.api.dto.ReviewDto;
import com.promptdex.api.security.UserPrincipal;
import com.promptdex.api.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/prompts") // This controller handles actions nested under prompts
@PreAuthorize("isAuthenticated()")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Handles the creation of a new review for a specific prompt.
     * POST /api/prompts/{promptId}/reviews
     */
    @PostMapping("/{promptId}/reviews")
    public ResponseEntity<ReviewDto> createReview(
            @PathVariable UUID promptId,
            @Valid @RequestBody CreateReviewRequest reviewRequest,
            @AuthenticationPrincipal UserPrincipal principal) {

        // --- FIX: Called the correct method 'createReview' instead of 'addReviewToPrompt'
        // --- and passed the full 'principal' object instead of just the username.
        ReviewDto newReview = reviewService.createReview(promptId, reviewRequest, principal);

        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }
}