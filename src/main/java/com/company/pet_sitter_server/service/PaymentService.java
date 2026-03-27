package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.dto.payment.PaymentRequest;
import com.company.pet_sitter_server.dto.payment.PaymentResponse;
import com.company.pet_sitter_server.entity.Payment;
import com.company.pet_sitter_server.enums.PaymentStatus;
import com.company.pet_sitter_server.repository.BookingRepository;
import com.company.pet_sitter_server.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private BookingRepository bookingRepository;

    public PaymentResponse createPayment(Long userId, PaymentRequest request) {
        bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionNo(request.getTransactionNo());
        payment.setTransactionDate(request.getTransactionDate());
        payment.setSlipImageUrl(request.getSlipImageUrl());
        payment.setStatus(PaymentStatus.PENDING);

        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponse getPaymentByBooking(Long bookingId, Long userId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse r = new PaymentResponse();
        r.setId(payment.getId());
        r.setBookingId(payment.getBookingId());
        r.setAmount(payment.getAmount());
        r.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        r.setPaymentMethod(payment.getPaymentMethod());
        r.setTransactionNo(payment.getTransactionNo());
        r.setTransactionDate(payment.getTransactionDate());
        r.setSlipImageUrl(payment.getSlipImageUrl());
        r.setCreatedAt(payment.getCreatedAt());
        return r;
    }
}
