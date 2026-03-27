package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.config.JwtHelper;
import com.company.pet_sitter_server.dto.review.ReviewRequest;
import com.company.pet_sitter_server.dto.review.ReviewResponse;
import com.company.pet_sitter_server.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired private ReviewService reviewService;
    @Autowired private JwtHelper jwtHelper;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(
                reviewService.createReview(jwtHelper.getCurrentUserId(), request));
    }

    // Public — no token required (handled by SecurityConfig)
    @GetMapping("/sitter/{sitterId}")
    public ResponseEntity<Page<ReviewResponse>> getSitterReviews(
            @PathVariable Long sitterId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsBySitter(sitterId, pageable));
    }
}
