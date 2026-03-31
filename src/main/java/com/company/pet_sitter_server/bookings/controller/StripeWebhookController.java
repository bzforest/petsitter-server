package com.company.pet_sitter_server.bookings.controller;

import com.company.pet_sitter_server.bookings.entity.Bookings;
import com.company.pet_sitter_server.bookings.repository.BookingRepository;
import com.company.pet_sitter_server.enums.BookingStatus;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final BookingRepository bookingRepository;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            // ตรวจสอบ Signature เพื่อความปลอดภัย (ป้องกันคนปลอมข้อมูล)
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            // จัดการเฉพาะตอนที่จ่ายเงินสำเร็จ (payment_intent.succeeded)
            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();

                // ค้นหา Booking ใน DB โดยใช้ client_secret ที่เก็บไว้ในตอนแรก
                Bookings booking = bookingRepository.findByStripePaymentIntentId(intent.getId())
                        .orElseThrow(() -> new RuntimeException("ไม่พบข้อมูลการจองสำหรับรหัสนี้"));

                // อัปเดตสถานะเป็น PAID
                booking.setStatus(BookingStatus.PAID);
                bookingRepository.save(booking);

                System.out.println("✅ จ่ายเงินสำเร็จ! อัปเดต Booking ID: " + booking.getId());
            }

            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            System.err.println("❌ Webhook error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Webhook error: " + e.getMessage());
        }
    }
}