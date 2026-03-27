package com.company.pet_sitter_server.repository;

import com.company.pet_sitter_server.entity.Booking;
import com.company.pet_sitter_server.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Booking> findBySitterIdOrderByCreatedAtDesc(Long sitterId, Pageable pageable);
    List<Booking> findBySitterId(Long sitterId);
    List<Booking> findByUserId(Long userId);
}
