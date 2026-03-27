package com.company.pet_sitter_server.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payout_options")
@Data
public class PayoutOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long sitterId;

    private String bankName;
    private String accountNumber;
    private String accountName;
    private String bookBankImageUrl;
}
