package com.company.pet_sitter_server.entity.sitter;

import jakarta.persistence.*;
import lombok.Data;

import com.company.pet_sitter_server.entity.service_type.ServiceType;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "sitter_services")
@Data
public class PetSitter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "sitter_id")
    private Long sitterId;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private ServiceType serviceType;

    @Column(name = "price_per_hour")
    private Double pricePerHour;

    @Column(name = "is_available")
    private Boolean isAvailable;
}
