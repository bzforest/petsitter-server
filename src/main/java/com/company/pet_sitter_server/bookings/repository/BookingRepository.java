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
    org.springframework.data.domain.Page<Bookings> findByUserIdOrderByCreatedAtDesc(Long userId,
            org.springframework.data.domain.Pageable pageable);

    // booking history ของ sitter
    org.springframework.data.domain.Page<Bookings> findBySitterIdOrderByCreatedAtDesc(Long sitterId,
            org.springframework.data.domain.Pageable pageable);

    // ดึง booking ทั้งหมดของ sitter (ใช้สำหรับ Payout - คำนวณรายได้)
    java.util.List<Bookings> findAllBySitterId(Long sitterId);

    // เช็คจองซ้อน (ดึงรายการที่ยัง Active ทั้งหมดของ Sitter)
    List<Bookings> findBySitterIdAndStatusIn(Long sitterId, List<com.company.pet_sitter_server.enums.BookingStatus> statuses);
}
