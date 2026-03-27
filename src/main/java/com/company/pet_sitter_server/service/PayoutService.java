package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.dto.payout.PayoutRequest;
import com.company.pet_sitter_server.dto.payout.PayoutResponse;
import com.company.pet_sitter_server.entity.PayoutOption;
import com.company.pet_sitter_server.enums.BookingStatus;
import com.company.pet_sitter_server.enums.PaymentStatus;
import com.company.pet_sitter_server.repository.BookingRepository;
import com.company.pet_sitter_server.repository.PaymentRepository;
import com.company.pet_sitter_server.repository.PayoutOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PayoutService {

    @Autowired private PayoutOptionRepository payoutOptionRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private PaymentRepository paymentRepository;

    public PayoutResponse getMyPayout(Long sitterId) {
        PayoutOption option = payoutOptionRepository.findBySitterId(sitterId)
                .orElse(new PayoutOption());
        return toResponse(option, sitterId);
    }

    public PayoutResponse updateMyPayout(Long sitterId, PayoutRequest request) {
        PayoutOption option = payoutOptionRepository.findBySitterId(sitterId)
                .orElseGet(() -> {
                    PayoutOption p = new PayoutOption();
                    p.setSitterId(sitterId);
                    return p;
                });
        option.setBankName(request.getBankName());
        option.setAccountNumber(request.getAccountNumber());
        option.setAccountName(request.getAccountName());
        option.setBookBankImageUrl(request.getBookBankImageUrl());
        return toResponse(payoutOptionRepository.save(option), sitterId);
    }

    private PayoutResponse toResponse(PayoutOption option, Long sitterId) {
        PayoutResponse r = new PayoutResponse();
        r.setId(option.getId());
        r.setSitterId(sitterId);
        r.setBankName(option.getBankName());
        r.setAccountNumber(option.getAccountNumber());
        r.setAccountName(option.getAccountName());
        r.setBookBankImageUrl(option.getBookBankImageUrl());

        Double totalEarning = bookingRepository.findBySitterId(sitterId).stream()
                .filter(b -> b.getStatus() == BookingStatus.SUCCESS)
                .mapToDouble(b -> {
                    return paymentRepository.findByBookingId(b.getId())
                            .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                            .map(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                            .orElse(0.0);
                })
                .sum();
        r.setTotalEarning(totalEarning);
        return r;
    }
}
