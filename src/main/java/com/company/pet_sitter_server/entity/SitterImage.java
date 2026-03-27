package com.company.pet_sitter_server.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sitter_images")
@Data
public class SitterImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sitterId;
    private String imageUrl;
}
