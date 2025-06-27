package com.promptdex.api.controller.admin;
import com.promptdex.api.dto.ReviewAdminViewDto;
import com.promptdex.api.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; 
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
    @GetMapping
    public ResponseEntity<Page<ReviewAdminViewDto>> getAllReviews(
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewAdminViewDto> reviews = reviewService.getAllReviewsAsAdmin(searchTerm, pageable);
        return ResponseEntity.ok(reviews);
    }
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReviewAsAdmin(reviewId);
        return ResponseEntity.noContent().build();
    }
}