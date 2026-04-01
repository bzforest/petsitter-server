package com.company.pet_sitter_server.user.dto;

import com.company.pet_sitter_server.enums.SitterStatus;

import java.time.LocalDate;
import java.util.List;

public class SitterProfileResponse {
    public Long id;
    public Long userId;
    public String email;
    public String bio;
    public Double pricePerHour;
    public Integer experience;
    public String tradeName;
    public String petTypes;
    public String services;
    public String placeDescription;
    public String phone;
    public String idNumber;
    public LocalDate dateOfBirth;
    public SitterStatus status;
    public Boolean isApproved;
    public Double ratingAvg;
    public String rejectReason;
    public Double latitude;
    public Double longitude;
    public List<String> gallery;

    // flat address fields จาก Address record
    public Long addressId;
    public String addressLine;
    public String district;
    public String subDistrict;
    public String province;
    public String postalCode;
}