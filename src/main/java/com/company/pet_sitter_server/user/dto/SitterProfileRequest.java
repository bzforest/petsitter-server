package com.company.pet_sitter_server.user.dto;

import java.time.LocalDate;

public class SitterProfileRequest {
    public Long userId;
    public String bio;
    public Double pricePerHour;
    public String experience;
    public Integer experienceYears;
    public String tradeName;
    public String petTypes;
    public String placeDescription;
    public String idNumber;
    public LocalDate dateOfBirth;
    public Long addressId;
}