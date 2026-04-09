package com.company.pet_sitter_server.bookings.service;

import com.company.pet_sitter_server.bookings.dto.StripePaymentResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    public StripePaymentResponse createPaymentIntent(Double amount) throws StripeException {
        long amountInCents = (long) (amount * 100);

        // ตั้งค่าข้อมูลการจ่ายเงิน
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("thb")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        // ส่งคำขอไปที่ Stripe API
        PaymentIntent intent = PaymentIntent.create(params);

        // ส่ง Client Secret กลับไป (ตัวนี้ Frontend ต้องใช้)
        StripePaymentResponse response = new StripePaymentResponse();
        response.setPaymentIntentId(intent.getId()); // "pi_xxx" ← เก็บใน DB
        response.setClientSecret(intent.getClientSecret()); // ← ส่งให้ Frontend
        return response;
    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }
}