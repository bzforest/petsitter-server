package com.company.pet_sitter_server.dto;

public class PetSitterRequest {
    private String fullName;
    private String experience;
    private Double pricePerHour;

    public String getFullName() { return fullName; }
    public String getExperience() { return experience; }
    public Double getPricePerHour() { return pricePerHour; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setExperience(String experience) { this.experience = experience; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }
}
