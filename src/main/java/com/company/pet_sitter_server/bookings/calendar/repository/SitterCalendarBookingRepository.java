package com.company.pet_sitter_server.bookings.calendar.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.company.pet_sitter_server.bookings.entity.Bookings;
import com.company.pet_sitter_server.enums.BookingStatus;

@Repository
public interface SitterCalendarBookingRepository extends JpaRepository<Bookings, Long> {
    @Query("""
            SELECT b
            FROM Bookings b
            WHERE b.sitterId = :sitterId
              AND b.startDate <= :endDate
              AND b.endDate >= :startDate
              AND b.status <> :excludedStatus
            ORDER BY b.startDate ASC, b.startTime ASC, b.id ASC
            """)
    List<Bookings> findCalendarBookingsBySitterAndDateRange(
            @Param("sitterId") Long sitterId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedStatus") BookingStatus excludedStatus);
}
