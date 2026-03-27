package com.company.pet_sitter_server.dto.pet;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PetRequest {
    @NotBlank(message = "Pet name is required")
    private String name;

    @NotBlank(message = "Pet type is required")
    private String type;

    private String breed;
    private String sex;

    @Min(value = 0, message = "Age cannot be negative")
    private Integer age;

    @Min(value = 0, message = "Weight cannot be negative")
    private Double weight;

    private String color;
    private String aboutPet;
    private String imageUrl;
}
