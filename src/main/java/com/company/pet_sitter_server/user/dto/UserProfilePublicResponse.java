package com.company.pet_sitter_server.user.dto;

import java.time.LocalDate;

public class UserProfilePublicResponse {
    public Long id;
    public Long userId;
    public String fullName;
    public String email;
    public String phone;
    public String profileImage;
    public String idNumber;
    public LocalDate dateOfBirth;
}
