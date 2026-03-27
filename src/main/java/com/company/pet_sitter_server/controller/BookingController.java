package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.config.JwtHelper;
import com.company.pet_sitter_server.dto.booking.BookingRequest;
import com.company.pet_sitter_server.dto.booking.BookingResponse;
import com.company.pet_sitter_server.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired private BookingService bookingService;
    @Autowired private JwtHelper jwtHelper;

    // ─── Pet Owner side ───────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(
                bookingService.createBooking(jwtHelper.getCurrentUserId(), request));
    }

    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getMyBookings(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                bookingService.getMyBookings(jwtHelper.getCurrentUserId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(
                bookingService.getBookingById(id, jwtHelper.getCurrentUserId()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(
                bookingService.cancelBooking(id, jwtHelper.getCurrentUserId()));
    }

    // ─── Sitter side (SecurityConfig + @PreAuthorize enforces SITTER role) ───

    @GetMapping("/sitter")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<Page<BookingResponse>> getSitterBookings(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                bookingService.getSitterBookings(jwtHelper.getCurrentUserId(), pageable));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id, jwtHelper.getCurrentUserId()));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<BookingResponse> rejectBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.rejectBooking(id, jwtHelper.getCurrentUserId()));
    }

    @PutMapping("/{id}/in-service")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<BookingResponse> startService(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.startService(id, jwtHelper.getCurrentUserId()));
    }

    @PutMapping("/{id}/success")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<BookingResponse> completeService(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.completeService(id, jwtHelper.getCurrentUserId()));
    }
}
