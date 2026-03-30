package com.company.pet_sitter_server.pets.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PetResponse {
    private Long id;
    private String name;
    private String type;
    private String breed;
    private String sex;
    private Integer age;
    private Double weight;
    private String aboutPet;
    private String imageUrl;
    private Long userId;
    private LocalDateTime createdAt;
}