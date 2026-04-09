package com.company.pet_sitter_server.bookings;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.company.pet_sitter_server.bookings.conflict.repository.BookingConflictRepository;
import com.company.pet_sitter_server.bookings.conflict.service.BookingConflictService;
import com.company.pet_sitter_server.bookings.entity.Bookings;
import com.company.pet_sitter_server.enums.BookingStatus;

@ExtendWith(MockitoExtension.class)
class BookingConflictServiceTest {

    @Mock
    private BookingConflictRepository bookingConflictRepository;

    @InjectMocks
    private BookingConflictService bookingConflictService;

    @Test
    void ensureNoOverlapForSitter_shouldPass_whenNoOverlapFound() {
        when(bookingConflictRepository.findOverlaps(
                eq(10L),
                eq(LocalDate.of(2026, 4, 10)),
                eq(LocalTime.of(8, 0)),
                eq(LocalDate.of(2026, 4, 10)),
                eq(LocalTime.of(10, 0)),
                any(),
                eq(101L)))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> bookingConflictService.ensureNoOverlapForSitter(
                10L,
                LocalDate.of(2026, 4, 10),
                LocalTime.of(8, 0),
                LocalDate.of(2026, 4, 10),
                LocalTime.of(10, 0),
                101L));
    }

    @Test
    void ensureNoOverlapForSitter_shouldThrow_whenOverlapFound() {
        Bookings existing = new Bookings();
        existing.setId(88L);

        when(bookingConflictRepository.findOverlaps(
                eq(10L),
                eq(LocalDate.of(2026, 4, 10)),
                eq(LocalTime.of(8, 0)),
                eq(LocalDate.of(2026, 4, 10)),
                eq(LocalTime.of(10, 0)),
                any(),
                eq(101L)))
                .thenReturn(List.of(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookingConflictService.ensureNoOverlapForSitter(
                        10L,
                        LocalDate.of(2026, 4, 10),
                        LocalTime.of(8, 0),
                        LocalDate.of(2026, 4, 10),
                        LocalTime.of(10, 0),
                        101L));

        assertEquals("Sitter already has another booking in this time range.", ex.getMessage());
    }

    @Test
    void ensureNoOverlapForSitter_shouldQueryOnlyActiveStatuses() {
        when(bookingConflictRepository.findOverlaps(
                eq(10L),
                eq(LocalDate.of(2026, 4, 10)),
                eq(LocalTime.of(8, 0)),
                eq(LocalDate.of(2026, 4, 10)),
                eq(LocalTime.of(10, 0)),
                any(),
                eq(101L)))
                .thenReturn(List.of());

        bookingConflictService.ensureNoOverlapForSitter(
                10L,
                LocalDate.of(2026, 4, 10),
                LocalTime.of(8, 0),
                LocalDate.of(2026, 4, 10),
                LocalTime.of(10, 0),
                101L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BookingStatus>> statusCaptor = ArgumentCaptor.forClass(List.class);

        verify(bookingConflictRepository).findOverlaps(
                eq(10L),
                eq(LocalDate.of(2026, 4, 10)),
                eq(LocalTime.of(8, 0)),
                eq(LocalDate.of(2026, 4, 10)),
                eq(LocalTime.of(10, 0)),
                statusCaptor.capture(),
                eq(101L));

        assertEquals(
                List.of(BookingStatus.PENDING, BookingStatus.PAID, BookingStatus.CONFIRMED),
                statusCaptor.getValue());
    }
}
