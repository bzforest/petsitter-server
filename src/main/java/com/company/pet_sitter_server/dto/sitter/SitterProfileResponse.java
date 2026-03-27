package com.company.pet_sitter_server.dto.sitter;

import com.company.pet_sitter_server.dto.pet.PetResponse;
import lombok.Data;
import java.util.List;

@Data
public class SitterProfileResponse {
    private Long id;
    private Long userId;
    private String email;
    private String tradeName;
    private String bio;
    private Integer experienceYears;
    private List<String> petTypes;
    private String phone;
    private String profileImage;
    private String placeDescription;
    private Double ratingAvg;
    private String status;
    private String rejectReason;
    private String addressLine;
    private String district;
    private String province;
    private String postalCode;
    private List<String> imageUrls;
    private List<SitterOfferResponse> services;
}
