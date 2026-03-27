package com.company.pet_sitter_server.repository;

import com.company.pet_sitter_server.entity.BookingPet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingPetRepository extends JpaRepository<BookingPet, Long> {
    List<BookingPet> findByBookingId(Long bookingId);
    void deleteByBookingId(Long bookingId);
}
