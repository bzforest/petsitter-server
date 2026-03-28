package com.company.pet_sitter_server.pets.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "pets")
@Data
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String name;
    private String type;
    private String breed;
    private String sex;
    private Integer age;
    private Double weight;

    @Column(name = "about_pet")
    private String aboutPet;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
