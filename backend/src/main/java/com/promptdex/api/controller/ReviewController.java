// src/main/java/com/promptdex/api/controller/ReviewController.java
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
@RequestMapping("/api/prompts/{promptId}/reviews") // Endpoint for actions related to a prompt
@PreAuthorize("isAuthenticated()")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewDto> createReviewForPrompt(
            @PathVariable UUID promptId,
            @Valid @RequestBody CreateReviewRequest reviewRequest,
            @AuthenticationPrincipal UserPrincipal principal) {

        // This is the corrected call.
        ReviewDto newReview = reviewService.addReviewToPrompt(promptId, reviewRequest, principal.getUsername());

        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }
}