package com.company.pet_sitter_server.entity;

import com.company.pet_sitter_server.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "text")
    private PaymentStatus status = PaymentStatus.PENDING;

    private String paymentMethod;
    private String transactionNo;
    private LocalDate transactionDate;
    private String slipImageUrl;
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
