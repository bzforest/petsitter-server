package com.company.pet_sitter_server.sitter_service.dto;

import lombok.Data;

@Data
public class SitterServiceResponse {
    private Long id;
    private Long sitterId;
    private Long serviceId;
    private String serviceName;
    private Double pricePerHour;
    private Boolean isAvailable;
}