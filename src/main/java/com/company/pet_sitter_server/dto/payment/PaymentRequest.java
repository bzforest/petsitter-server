package com.company.pet_sitter_server.dto.payment;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PaymentRequest {
    private Long bookingId;
    private Double amount;
    private String paymentMethod;
    private String transactionNo;
    private LocalDate transactionDate;
    private String slipImageUrl;
}
