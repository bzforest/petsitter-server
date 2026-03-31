package com.company.pet_sitter_server.bookings.controller;

import com.company.pet_sitter_server.bookings.dto.BookingRequest;
import com.company.pet_sitter_server.bookings.dto.BookingResponse;
import com.company.pet_sitter_server.bookings.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // POST /api/bookings — สร้าง booking ใหม่
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.ok(response);
    }

    // GET /api/bookings/{id} — ดึง booking detail (Success Page)
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    // GET /api/bookings/user/{userId} — ดึง booking history ของ user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    // PATCH /api/bookings/{id}/confirm-cash — ยืนยัน Cash payment → PAID
    @PatchMapping("/{id}/confirm-cash")
    public ResponseEntity<BookingResponse> confirmCash(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmCash(id));
    }

    // PATCH /api/bookings/{id}/cancel — ยกเลิก booking
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }
}