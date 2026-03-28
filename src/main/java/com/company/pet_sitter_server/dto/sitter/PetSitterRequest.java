package com.company.pet_sitter_server.dto.sitter;

import lombok.Data;

@Data
public class PetSitterRequest {
    private Long sitterId;
    private Long serviceId;
    private Double pricePerHour;
    private Boolean isAvailable;
}
