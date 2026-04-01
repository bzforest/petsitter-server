package com.company.pet_sitter_server.user.dto;

import com.company.pet_sitter_server.enums.SitterStatus;

import java.time.LocalDate;

public class SitterProfileResponse {
    public Long id;
    public Long userId;
    public String bio;
    public Double pricePerHour;
    public String experience;
    public Integer experienceYears;
    public String tradeName;
    public String petTypes;
    public String placeDescription;
    public String phone;
    public String idNumber;
    public LocalDate dateOfBirth;
    public SitterStatus status;
    public Boolean isApproved;
    public Double ratingAvg;
    public String rejectReason;
    public Long addressId;
}