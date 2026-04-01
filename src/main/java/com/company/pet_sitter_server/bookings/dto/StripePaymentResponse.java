package com.company.pet_sitter_server.bookings.dto;

import lombok.Data;

@Data
public class StripePaymentResponse {
    private String paymentIntentId; // "pi_xxx" เก็บใน DB
    private String clientSecret;    // ส่งให้ Frontend ใช้กับ Stripe Elements
}
