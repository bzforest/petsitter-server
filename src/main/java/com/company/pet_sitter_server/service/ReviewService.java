package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.dto.review.ReviewRequest;
import com.company.pet_sitter_server.dto.review.ReviewResponse;
import com.company.pet_sitter_server.entity.Booking;
import com.company.pet_sitter_server.entity.Review;
import com.company.pet_sitter_server.enums.BookingStatus;
import com.company.pet_sitter_server.exception.AccessDeniedException;
import com.company.pet_sitter_server.exception.BadRequestException;
import com.company.pet_sitter_server.exception.NotFoundException;
import com.company.pet_sitter_server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private SitterProfileRepository sitterProfileRepository;

    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only review your own bookings");
        }
        if (booking.getStatus() != BookingStatus.SUCCESS) {
            throw new BadRequestException("Can only review completed bookings");
        }
        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new BadRequestException("You have already reviewed this booking");
        }

        Review review = new Review();
        review.setBookingId(request.getBookingId());
        review.setUserId(userId);
        review.setSitterId(booking.getSitterId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        Review saved = reviewRepository.save(review);

        updateSitterRating(booking.getSitterId());
        return toResponse(saved);
    }

    public Page<ReviewResponse> getReviewsBySitter(Long sitterId, Pageable pageable) {
        return reviewRepository.findBySitterId(sitterId, pageable).map(this::toResponse);
    }

    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + reviewId));
        reviewRepository.deleteById(reviewId);
        updateSitterRating(review.getSitterId());
    }

    private void updateSitterRating(Long sitterId) {
        Double avg = reviewRepository.findAvgRatingBySitterId(sitterId);
        sitterProfileRepository.findByUserId(sitterId).ifPresent(profile -> {
            profile.setRatingAvg(avg != null ? avg : 0.0);
            sitterProfileRepository.save(profile);
        });
    }

    public ReviewResponse toResponse(Review review) {
        ReviewResponse r = new ReviewResponse();
        r.setId(review.getId());
        r.setBookingId(review.getBookingId());
        r.setUserId(review.getUserId());
        r.setSitterId(review.getSitterId());
        r.setRating(review.getRating());
        r.setComment(review.getComment());
        r.setCreatedAt(review.getCreatedAt());
        userProfileRepository.findByUserId(review.getUserId()).ifPresent(up -> {
            r.setReviewerName(up.getFullName());
            r.setReviewerImage(up.getProfileImage());
        });
        return r;
    }
}
