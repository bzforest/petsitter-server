package com.company.pet_sitter_server.dto;

import lombok.Data;

@Data
public class PetSitterRequest {
    private String fullName;
    private String experience;
    private Double pricePerHour;
}