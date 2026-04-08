package com.company.pet_sitter_server.bookings.controller;

import com.company.pet_sitter_server.bookings.dto.BookingResponse;
import com.company.pet_sitter_server.bookings.entity.Bookings;
import com.company.pet_sitter_server.bookings.repository.BookingRepository;
import com.company.pet_sitter_server.bookings.service.BookingService;
import com.company.pet_sitter_server.common.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sitter-only decline path: existing {@code /cancel} is owner-only in {@link BookingController}.
 * This endpoint lets the logged-in sitter cancel (same DB status CANCELLED) without modifying teammate files.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class SitterBookingDeclineController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final JwtUtil jwtUtil;

    @PatchMapping("/{id}/decline-by-sitter")
    public ResponseEntity<BookingResponse> declineBySitter(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || authHeader.length() < 8) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);
        if (!"SITTER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Long sitterUserId = jwtUtil.extractUserId(token);

        Bookings booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));
        if (!booking.getSitterId().equals(sitterUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }
}
