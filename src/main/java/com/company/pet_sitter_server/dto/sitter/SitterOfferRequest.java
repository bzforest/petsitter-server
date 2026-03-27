package com.company.pet_sitter_server.dto.sitter;

import lombok.Data;

@Data
public class SitterOfferRequest {
    private Long serviceId;
    private Double pricePerHour;
    private Boolean isAvailable;
}
