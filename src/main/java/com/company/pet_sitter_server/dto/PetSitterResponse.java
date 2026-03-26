package com.company.pet_sitter_server.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class PetSitterResponse {
    private UUID id;
    private String fullName;
    private String experience;
    private Double pricePerHour;
}
