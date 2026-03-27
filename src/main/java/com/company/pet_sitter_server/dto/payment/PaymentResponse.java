package com.company.pet_sitter_server.dto.payment;

import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long bookingId;
    private Double amount;
    private String status;
    private String paymentMethod;
    private String transactionNo;
    private LocalDate transactionDate;
    private String slipImageUrl;
    private OffsetDateTime createdAt;
}
