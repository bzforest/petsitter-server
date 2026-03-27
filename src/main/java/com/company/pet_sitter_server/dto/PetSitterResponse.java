package com.company.pet_sitter_server.dto;

import java.util.UUID;

public class PetSitterResponse {
    private UUID id;
    private String fullName;
    private String experience;
    private Double pricePerHour;

    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public String getExperience() { return experience; }
    public Double getPricePerHour() { return pricePerHour; }

    public void setId(UUID id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setExperience(String experience) { this.experience = experience; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }
}
