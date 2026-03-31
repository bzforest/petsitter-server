package com.company.pet_sitter_server.bookings.controller;

import com.company.pet_sitter_server.bookings.dto.BookingRequest;
import com.company.pet_sitter_server.bookings.dto.BookingResponse;
import com.company.pet_sitter_server.bookings.service.BookingService;
import com.company.pet_sitter_server.common.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final JwtUtil jwtUtil;

    // validation
    private Long extractUserIdFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);
        return jwtUtil.extractUserId(token);
    }

    // POST /api/bookings — สร้าง booking ใหม่
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
        @RequestBody BookingRequest request,
        HttpServletRequest httpRequest) {
        
        Long userId = extractUserIdFromToken(httpRequest);
        request.setUserId(userId);
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
    public ResponseEntity<BookingResponse> confirmCash(
        @PathVariable Long id,
        HttpServletRequest httpServletRequest) {

        String role = jwtUtil.extractRole(
            httpServletRequest.getHeader("Authorization").substring(7)
        );
        if (!role.equals("SITTER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(bookingService.confirmCash(id));
    }

    // PATCH /api/bookings/{id}/cancel — ยกเลิก booking
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
        @PathVariable Long id,
        HttpServletRequest httpRequest) {
        
        Long userId = extractUserIdFromToken(httpRequest);
        return ResponseEntity.ok(bookingService.cancelBooking(id, userId));
    }
}