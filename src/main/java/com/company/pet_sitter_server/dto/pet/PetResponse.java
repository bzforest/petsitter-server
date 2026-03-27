package com.company.pet_sitter_server.dto.pet;

import lombok.Data;

@Data
public class PetResponse {
    private Long id;
    private Long userId;
    private String name;
    private String type;
    private String breed;
    private String sex;
    private Integer age;
    private Double weight;
    private String color;
    private String aboutPet;
    private String imageUrl;
}
