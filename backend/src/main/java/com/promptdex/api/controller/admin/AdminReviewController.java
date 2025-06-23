package com.promptdex.api.controller.admin;

import com.promptdex.api.dto.ReviewAdminViewDto;
import com.promptdex.api.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // Import Sort
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * GET /api/admin/reviews : Get a paginated list of all reviews, optionally filtered by search term.
     *
     * @param searchTerm Optional term to search by comment, author username, or prompt title.
     * @param pageable   Pagination information (e.g., ?page=0&size=10&sort=createdAt,desc).
     * @return A Page of ReviewAdminViewDto.
     */
    @GetMapping
    public ResponseEntity<Page<ReviewAdminViewDto>> getAllReviews(
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewAdminViewDto> reviews = reviewService.getAllReviewsAsAdmin(searchTerm, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * DELETE /api/admin/reviews/{reviewId} : Delete any review by its ID.
     *
     * @param reviewId The UUID of the review to delete.
     * @return ResponseEntity with status 204 No Content.
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReviewAsAdmin(reviewId);
        return ResponseEntity.noContent().build();
    }
}