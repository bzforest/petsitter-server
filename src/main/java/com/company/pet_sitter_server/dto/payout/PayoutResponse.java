package com.company.pet_sitter_server.dto.payout;

import lombok.Data;

@Data
public class PayoutResponse {
    private Long id;
    private Long sitterId;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String bookBankImageUrl;
    private Double totalEarning;
}
