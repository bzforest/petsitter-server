package com.company.pet_sitter_server.dto.user;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileResponse {
    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private String profileImage;
    private String idNumber;
    private LocalDate dateOfBirth;
    private String role;
}
