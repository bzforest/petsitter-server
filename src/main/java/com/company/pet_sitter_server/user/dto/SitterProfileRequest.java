package com.company.pet_sitter_server.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.List;

public class SitterProfileRequest {
    public Long userId;
    public String bio;
    public Double pricePerHour;
    @Min(0) @Max(100)
    public Integer experience;
    public String tradeName;
    public String petTypes;
    public String placeDescription;
    public String idNumber;
    public LocalDate dateOfBirth;
    public Long addressId;
    public List<String> gallery;
    public String servicesDescription;
}