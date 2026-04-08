package com.company.pet_sitter_server.bookings.conflict.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.company.pet_sitter_server.bookings.conflict.repository.BookingConflictRepository;
import com.company.pet_sitter_server.enums.BookingStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingConflictService {
    private final BookingConflictRepository bookingConflictRepository;

    public void ensureNoOverlapForSitter(
            Long sitterId,
            LocalDate startDate,
            LocalTime startTime,
            LocalDate endDate,
            LocalTime endTime,
            Long excludeBookingId) {
        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.PENDING,
                BookingStatus.PAID,
                BookingStatus.CONFIRMED);

        boolean hasConflict = !bookingConflictRepository.findOverlaps(
                sitterId,
                startDate,
                startTime,
                endDate,
                endTime,
                activeStatuses,
                excludeBookingId).isEmpty();

        if (hasConflict) {
            throw new IllegalArgumentException("Sitter already has another booking in this time range.");
        }
    }
}
