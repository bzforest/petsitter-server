package com.company.pet_sitter_server.dto.sitter;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SitterProfileRequest {
    private String tradeName;
    private String bio;
    private Integer experienceYears;
    private List<String> petTypes;
    private String phone;
    private String idNumber;
    private LocalDate dateOfBirth;
    private String placeDescription;
    private String addressLine;
    private String district;
    private String province;
    private String postalCode;
}
