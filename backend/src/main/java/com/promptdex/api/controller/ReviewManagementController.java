// src/main/java/com/promptdex/api/controller/ReviewManagementController.java
package com.promptdex.api.controller;

import com.promptdex.api.dto.ReviewDto;
import com.promptdex.api.dto.UpdateReviewRequest;
import com.promptdex.api.security.UserPrincipal;
import com.promptdex.api.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews") // A separate endpoint for direct review management
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
            @AuthenticationPrincipal UserPrincipal principal) {

        ReviewDto updatedReview = reviewService.updateReview(reviewId, reviewRequest, principal.getUsername());
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal UserPrincipal principal) {

        reviewService.deleteReview(reviewId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}