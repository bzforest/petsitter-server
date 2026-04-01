package com.company.pet_sitter_server.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

public class SitterProfileUpdateRequest {
    public String bio;
    public Double pricePerHour;
    @Min(0) @Max(100)
    public Integer experience;

    @Pattern(
            regexp = "^\\d{10}$",
            message = "Phone number must be exactly 10 digits."
    )
    public String phone;

    @Email(
            message = "Enter a valid email address. It must include @ and a domain (e.g. name@example.com)."
    )
    public String email;
    public String tradeName;
    public String petTypes;
    public String services;
    public String placeDescription;
    public String idNumber;
    public LocalDate dateOfBirth;
    public Double latitude;
    public Double longitude;
    public List<String> gallery;

    // flat address fields — สร้าง/อัปเดต Address record อัตโนมัติ
    public String addressLine;
    public String district;
    public String subDistrict;
    public String province;
    public String postalCode;
}
