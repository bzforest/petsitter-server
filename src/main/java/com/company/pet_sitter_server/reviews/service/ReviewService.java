package com.company.pet_sitter_server.reviews.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.pet_sitter_server.bookings.entity.Bookings;
import com.company.pet_sitter_server.bookings.repository.BookingRepository;
import com.company.pet_sitter_server.enums.BookingStatus;
import com.company.pet_sitter_server.enums.Role;
import com.company.pet_sitter_server.reviews.dto.ReviewRequestDTO;
import com.company.pet_sitter_server.reviews.dto.ReviewResponseDTO;
import com.company.pet_sitter_server.reviews.entity.Review;
import com.company.pet_sitter_server.reviews.repository.ReviewRepository;
import com.company.pet_sitter_server.user.entity.SitterProfile;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.entity.UserProfile;
import com.company.pet_sitter_server.user.repository.SitterProfileRepository;
import com.company.pet_sitter_server.user.repository.UserProfileRepository;
import com.company.pet_sitter_server.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingsRepository;
    private final SitterProfileRepository sitterProfileRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponseDTO submitReview(ReviewRequestDTO request, Long currentUserId) {
        // Validation
        Bookings booking = bookingsRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(currentUserId)) {
            throw new RuntimeException("You can only review your own bookings");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException("You can only review completed services");
        }

        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new RuntimeException("You have already reviewed this booking");
        }

        // Save Review
        Review review = new Review();
        review.setBookingId(request.getBookingId());
        review.setUserId(currentUserId);
        review.setSitterId(booking.getSitterId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review = reviewRepository.save(review);

        // Update Sitter Average Rating
        updateSitterAverageRating(booking.getSitterId());

        return mapToResponse(review);
    }

    // Update Review
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewRequestDTO request, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUserId().equals(currentUserId)) {
            throw new RuntimeException("You can only edit your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review = reviewRepository.save(review);

        updateSitterAverageRating(review.getSitterId());

        return mapToResponse(review);
    }

    // Delete Review
    @Transactional
    public void deleteReview(Long reviewId, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only admins can delete reviews");
        }

        Long sitterId = review.getSitterId();
        reviewRepository.delete(review);

        updateSitterAverageRating(sitterId);
    }

    // Get Reviews by Sitter
    public Page<ReviewResponseDTO> getReviewsBySitter(Long sitterId, Pageable pageable) {
        return reviewRepository.findBySitterId(sitterId, pageable)
                .map(this::mapToResponse);
    }

    // Get Review by Booking ID
    public ReviewResponseDTO getReviewByBookingId(Long bookingId) {
        return reviewRepository.findByBookingId(bookingId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    // Update Sitter Average Rating
    private void updateSitterAverageRating(Long sitterUserId) {
        Double avg = reviewRepository.calculateAverageRatingBySitterId(sitterUserId);
        if (avg == null) avg = 0.0;

        SitterProfile profile = sitterProfileRepository.findByUserId(sitterUserId)
                .orElseThrow(() -> new RuntimeException("Sitter profile not found"));
        
        profile.setRatingAvg(avg);
        sitterProfileRepository.save(profile);
    }

    // Map Review to Response
    private ReviewResponseDTO mapToResponse(Review review) {
        UserProfile userProfile = userProfileRepository.findByUserId(review.getUserId())
                .orElse(null);

        return ReviewResponseDTO.builder()
                .id(review.getId())
                .bookingId(review.getBookingId())
                .userId(review.getUserId())
                .userName(userProfile != null ? userProfile.getFullName() : "Unknown User")
                .userProfileImage(userProfile != null ? userProfile.getProfileImage() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
