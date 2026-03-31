package com.company.pet_sitter_server.bookings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.company.pet_sitter_server.bookings.entity.Bookings;

@Repository
public interface BookingRepository extends JpaRepository<Bookings, Long> {
    // stripe webhook
    Optional<Bookings> findByStripePaymentIntentId(String stripePaymentIntentId);

    // booking history ของ user
    List<Bookings> findByUserIdOrderByCreatedAtDesc(Long userId);

    // booking history ของ sitter
    List<Bookings> findBySitterIdOrderByCreatedAtDesc(Long sitterId);
}
