package com.company.pet_sitter_server.reviews.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.pet_sitter_server.common.security.JwtUtil;
import com.company.pet_sitter_server.reviews.dto.ReviewRequestDTO;
import com.company.pet_sitter_server.reviews.dto.ReviewResponseDTO;
import com.company.pet_sitter_server.reviews.service.ReviewService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;

    // Extract User ID from Token
    private Long extractUserIdFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new RuntimeException("Unauthorized");
    }

    // Submit Review
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> submitReview(
            @RequestBody ReviewRequestDTO requestDTO,
            HttpServletRequest httpRequest) {
        Long userId = extractUserIdFromToken(httpRequest);
        return ResponseEntity.ok(reviewService.submitReview(requestDTO, userId));
    }

    // Update Review
    @PatchMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long id,
            @RequestBody ReviewRequestDTO requestDTO,
            HttpServletRequest httpRequest) {
        Long userId = extractUserIdFromToken(httpRequest);
        return ResponseEntity.ok(reviewService.updateReview(id, requestDTO, userId));
    }

    // Delete Review
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = extractUserIdFromToken(httpRequest);
        reviewService.deleteReview(id, userId);
        return ResponseEntity.noContent().build();
    }

    // Get Reviews by Sitter
    @GetMapping("/sitter/{sitterId}")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviewsBySitter(
            @PathVariable Long sitterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reviewService.getReviewsBySitter(sitterId, pageable));
    }

    // Get Review by Booking ID
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ReviewResponseDTO> getReviewByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(reviewService.getReviewByBookingId(bookingId));
    }
}
