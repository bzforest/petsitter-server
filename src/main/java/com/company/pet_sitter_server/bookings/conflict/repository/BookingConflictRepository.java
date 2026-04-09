package com.company.pet_sitter_server.bookings.conflict.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.company.pet_sitter_server.bookings.entity.Bookings;
import com.company.pet_sitter_server.enums.BookingStatus;

@Repository
public interface BookingConflictRepository extends JpaRepository<Bookings, Long> {

    @Query("""
            SELECT b
            FROM Bookings b
            WHERE b.sitterId = :sitterId
              AND b.status IN :activeStatuses
              AND (:excludeBookingId IS NULL OR b.id <> :excludeBookingId)
              AND (
                (b.startDate < :newEndDate OR (b.startDate = :newEndDate AND b.startTime < :newEndTime))
                AND
                (b.endDate > :newStartDate OR (b.endDate = :newStartDate AND b.endTime > :newStartTime))
              )
            """)
    List<Bookings> findOverlaps(
            @Param("sitterId") Long sitterId,
            @Param("newStartDate") LocalDate newStartDate,
            @Param("newStartTime") LocalTime newStartTime,
            @Param("newEndDate") LocalDate newEndDate,
            @Param("newEndTime") LocalTime newEndTime,
            @Param("activeStatuses") List<BookingStatus> activeStatuses,
            @Param("excludeBookingId") Long excludeBookingId);
}
