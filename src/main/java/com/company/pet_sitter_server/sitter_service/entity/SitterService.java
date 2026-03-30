package com.company.pet_sitter_server.sitter_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import com.company.pet_sitter_server.service_type.entity.ServiceType;

@Entity
@Table(name = "sitter_services")
@Data
public class SitterService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
