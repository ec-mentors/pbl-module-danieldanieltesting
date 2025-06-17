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

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewDto> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // The controller's job is simple: pass the data to the service
        ReviewDto createdReview = reviewService.createReview(request, userDetails.getUsername());

        // Return the newly created review DTO with a 201 Created status
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }
}