package com.company.pet_sitter_server.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "pet_sitters")
@Data
public class PetSitter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String fullName;
    private String experience;
    private Double pricePerHour;
}
