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

    // GET /api/bookings/user/me — ดึง booking history ของ user
    @GetMapping("/user/me")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(
            HttpServletRequest httpRequest) {
        Long userId = extractUserIdFromToken(httpRequest);
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    // GET /api/bookings/sitter/me — ดึงรายการจองที่ Sitter คนนี้ถูกจอง
    @GetMapping("/sitter/me")
    public ResponseEntity<List<BookingResponse>> getBookingsBySitter(
            HttpServletRequest httpRequest) {

        String role = jwtUtil.extractRole(
                httpRequest.getHeader("Authorization").substring(7));
        if (!role.equals("SITTER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long sitterId = extractUserIdFromToken(httpRequest);
        return ResponseEntity.ok(bookingService.getBookingsBySitter(sitterId));
    }


    // PATCH /api/bookings/{id}/cancel — ยกเลิก booking
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        Long userId = extractUserIdFromToken(httpRequest);
        BookingResponse booking = bookingService.getBookingById(id);
        if (!booking.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    // PATCH /api/bookings/{id}/confirm — Sitter ยอมรับงาน (PENDING/PAID ->
    // CONFIRMED)
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        String role = jwtUtil.extractRole(
                httpRequest.getHeader("Authorization").substring(7));
        if (!"SITTER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long sitterId = extractUserIdFromToken(httpRequest);
        return ResponseEntity.ok(bookingService.confirmBooking(id, sitterId));
    }

    // PATCH /api/bookings/{id}/complete — Sitter กดจบงาน -> COMPLETED
    @PatchMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> completeBooking(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        String role = jwtUtil.extractRole(
                httpRequest.getHeader("Authorization").substring(7));
        if (!"SITTER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long sitterId = extractUserIdFromToken(httpRequest);
        return ResponseEntity.ok(bookingService.completeBooking(id, sitterId));
    }

    // PATCH /api/bookings/{id}/verify-payment — ตรวจสอบผลการชำระเงินกับ Stripe
    @PatchMapping("/{id}/verify-payment")
    public ResponseEntity<BookingResponse> verifyPayment(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.verifyPayment(id));
    }
}