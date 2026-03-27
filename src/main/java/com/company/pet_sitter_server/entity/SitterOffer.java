package com.company.pet_sitter_server.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sitter_services")
@Data
public class SitterOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sitterId;
    private Long serviceId;
    private Double pricePerHour;
    private Boolean isAvailable = true;
}
