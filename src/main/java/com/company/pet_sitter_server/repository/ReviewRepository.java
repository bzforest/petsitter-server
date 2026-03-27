package com.company.pet_sitter_server.repository;

import com.company.pet_sitter_server.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findBySitterId(Long sitterId, Pageable pageable);
    Optional<Review> findByBookingId(Long bookingId);
    boolean existsByBookingId(Long bookingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.sitterId = :sitterId")
    Double findAvgRatingBySitterId(@Param("sitterId") Long sitterId);
}
