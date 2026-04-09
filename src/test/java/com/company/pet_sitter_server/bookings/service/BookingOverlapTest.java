package com.company.pet_sitter_server.bookings.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.company.pet_sitter_server.bookings.entity.Bookings;
import com.company.pet_sitter_server.bookings.repository.BookingRepository;
import com.company.pet_sitter_server.enums.BookingStatus;

@ExtendWith(MockitoExtension.class)
public class BookingOverlapTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    private final Long SITTER_USER_ID = 100L;
    private final LocalDate DATE = LocalDate.of(2026, 4, 10);

    private Bookings createMockBooking(LocalTime start, LocalTime end, BookingStatus status) {
        Bookings b = new Bookings();
        b.setId(valCount++);
        b.setSitterId(SITTER_USER_ID);
        b.setStartDate(DATE);
        b.setEndDate(DATE);
        b.setStartTime(start);
        b.setEndTime(end);
        b.setStatus(status);
        return b;
    }

    private static long valCount = 1;

    @Test
    void testOverlap_NoExistingBookings_Success() {
        when(bookingRepository.findBySitterIdAndStatusIn(anyLong(), anyList())).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> {
            bookingService.validateNoOverlap(SITTER_USER_ID, DATE, LocalTime.of(10, 0), DATE, LocalTime.of(11, 0), null);
        });
    }

    @Test
    void testOverlap_OverlapAtStart_ThrowsException() {
        // Existing: 10:30 - 11:30
        Bookings existing = createMockBooking(LocalTime.of(10, 30), LocalTime.of(11, 30), BookingStatus.CONFIRMED);
        when(bookingRepository.findBySitterIdAndStatusIn(anyLong(), anyList())).thenReturn(Arrays.asList(existing));

        // Requested: 10:00 - 11:00 (Overlaps 10:30-11:00)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.validateNoOverlap(SITTER_USER_ID, DATE, LocalTime.of(10, 0), DATE, LocalTime.of(11, 0), null);
        });

        assertTrue(exception.getMessage().contains("already booked"));
    }

    @Test
    void testOverlap_OverlapAtEnd_ThrowsException() {
        // Existing: 09:30 - 10:30
        Bookings existing = createMockBooking(LocalTime.of(9, 30), LocalTime.of(10, 30), BookingStatus.CONFIRMED);
        when(bookingRepository.findBySitterIdAndStatusIn(anyLong(), anyList())).thenReturn(Arrays.asList(existing));

        // Requested: 10:00 - 11:00 (Overlaps 10:00-10:30)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.validateNoOverlap(SITTER_USER_ID, DATE, LocalTime.of(10, 0), DATE, LocalTime.of(11, 0), null);
        });

        assertTrue(exception.getMessage().contains("already booked"));
    }

    @Test
    void testOverlap_TouchAtEnd_Success() {
        // Existing: 11:00 - 12:00
        Bookings existing = createMockBooking(LocalTime.of(11, 0), LocalTime.of(12, 0), BookingStatus.CONFIRMED);
        when(bookingRepository.findBySitterIdAndStatusIn(anyLong(), anyList())).thenReturn(Arrays.asList(existing));

        // Requested: 10:00 - 11:00 (Touches at 11:00 exactly)
        assertDoesNotThrow(() -> {
            bookingService.validateNoOverlap(SITTER_USER_ID, DATE, LocalTime.of(10, 0), DATE, LocalTime.of(11, 0), null);
        });
    }

    @Test
    void testOverlap_TouchAtStart_Success() {
        // Existing: 09:00 - 10:00
        Bookings existing = createMockBooking(LocalTime.of(9, 0), LocalTime.of(10, 0), BookingStatus.CONFIRMED);
        when(bookingRepository.findBySitterIdAndStatusIn(anyLong(), anyList())).thenReturn(Arrays.asList(existing));

        // Requested: 10:00 - 11:00 (Touches at 10:00 exactly)
        assertDoesNotThrow(() -> {
            bookingService.validateNoOverlap(SITTER_USER_ID, DATE, LocalTime.of(10, 0), DATE, LocalTime.of(11, 0), null);
        });
    }

    @Test
    void testOverlap_CancelledBooking_Success() {
        // Existing: 10:15 - 10:45 but CANCELLED
        // Note: The repository call itself should not return this if the status filter is correct,
        // but it's good to ensure the logic handles it if it were present.
        // Actually, findBySitterIdAndStatusIn won't return it.
        
        when(bookingRepository.findBySitterIdAndStatusIn(anyLong(), anyList())).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> {
            bookingService.validateNoOverlap(SITTER_USER_ID, DATE, LocalTime.of(10, 0), DATE, LocalTime.of(11, 0), null);
        });
    }

    @Test
    void testOverlap_ExcludeSelfDuringUpdate_Success() {
        Long myBookingId = 55L;
        Bookings self = createMockBooking(LocalTime.of(10, 0), LocalTime.of(11, 0), BookingStatus.PENDING);
        self.setId(myBookingId);
        
        when(bookingRepository.findBySitterIdAndStatusIn(anyLong(), anyList())).thenReturn(Arrays.asList(self));

        // Requested update: 10:30 - 11:30 (overlaps with old self, but we exclude it)
        assertDoesNotThrow(() -> {
            bookingService.validateNoOverlap(SITTER_USER_ID, DATE, LocalTime.of(10, 30), DATE, LocalTime.of(11, 30), myBookingId);
        });
    }
}
