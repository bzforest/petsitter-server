package com.company.pet_sitter_server.service_type.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "services")
@Data
public class ServiceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;
}
