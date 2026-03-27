package com.company.pet_sitter_server.pet_sitter.entity;

import jakarta.persistence.*;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "pet_sitters")
public class PetSitter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    private String fullName;
    private String experience;
    private Double pricePerHour;

    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public String getExperience() { return experience; }
    public Double getPricePerHour() { return pricePerHour; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setExperience(String experience) { this.experience = experience; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }
}
