package com.promptdex.api.controller;

import com.promptdex.api.dto.CreateReviewRequest;
import com.promptdex.api.dto.ReviewDto;
import com.promptdex.api.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/prompts")
@PreAuthorize("isAuthenticated()")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{promptId}/reviews")
    public ResponseEntity<ReviewDto> createReview(
            @PathVariable UUID promptId,
            @Valid @RequestBody CreateReviewRequest reviewRequest,
            @AuthenticationPrincipal UserDetails principal) {
        ReviewDto newReview = reviewService.createReview(promptId, reviewRequest, principal);
        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }
}