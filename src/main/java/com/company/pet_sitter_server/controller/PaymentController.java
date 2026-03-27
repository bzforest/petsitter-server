package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.config.JwtHelper;
import com.company.pet_sitter_server.dto.payment.PaymentRequest;
import com.company.pet_sitter_server.dto.payment.PaymentResponse;
import com.company.pet_sitter_server.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired private PaymentService paymentService;
    @Autowired private JwtHelper jwtHelper;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(
                paymentService.createPayment(jwtHelper.getCurrentUserId(), request));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getPaymentByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(
                paymentService.getPaymentByBooking(bookingId, jwtHelper.getCurrentUserId()));
    }
}
