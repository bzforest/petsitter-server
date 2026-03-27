package com.company.pet_sitter_server.dto.auth;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String role;
    private String fullName;
}
