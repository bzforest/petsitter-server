package com.company.pet_sitter_server.dto.sitter;

import lombok.Data;

@Data
public class SitterOfferResponse {
    private Long id;
    private Long serviceId;
    private String serviceName;
    private Double pricePerHour;
    private Boolean isAvailable;
}
