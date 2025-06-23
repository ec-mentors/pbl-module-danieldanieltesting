package com.promptdex.api.controller;

import com.promptdex.api.dto.ReviewDto;
import com.promptdex.api.dto.UpdateReviewRequest;
import com.promptdex.api.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // <-- FIX
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@PreAuthorize("isAuthenticated()")
public class ReviewManagementController {

    private final ReviewService reviewService;

    public ReviewManagementController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest reviewRequest,
            @AuthenticationPrincipal UserDetails principal) { // <-- FIX
        ReviewDto updatedReview = reviewService.updateReview(reviewId, reviewRequest, principal);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal UserDetails principal) { // <-- FIX
        reviewService.deleteReview(reviewId, principal);
        return ResponseEntity.noContent().build();
    }
}