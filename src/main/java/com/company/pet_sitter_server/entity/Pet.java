package com.company.pet_sitter_server.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pets")
@Data
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
