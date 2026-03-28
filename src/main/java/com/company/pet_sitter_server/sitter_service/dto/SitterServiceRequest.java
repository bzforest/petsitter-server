package com.company.pet_sitter_server.sitter_service.dto;

import lombok.Data;

@Data
public class SitterServiceRequest {
    private Long sitterId;
    private Long serviceId;
    private Double pricePerHour;
    private Boolean isAvailable;
}
