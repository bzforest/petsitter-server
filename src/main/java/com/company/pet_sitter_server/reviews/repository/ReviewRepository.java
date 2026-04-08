package com.company.pet_sitter_server.reviews.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.company.pet_sitter_server.reviews.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Page<Review> findBySitterId(Long sitterId, Pageable pageable);

    Page<Review> findBySitterIdAndRating(Long sitterId, Integer rating, Pageable pageable);
    
    Optional<Review> findByBookingId(Long bookingId);
    
    boolean existsByBookingId(Long bookingId);

    java.util.List<Review> findAllByBookingIdIn(java.util.Collection<Long> bookingIds);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.sitterId = :sitterId")
    Double calculateAverageRatingBySitterId(@Param("sitterId") Long sitterId);
}
