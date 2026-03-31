package com.company.pet_sitter_server.auth.dto;

// สิ่งที่ส่งกลับไปให้ client หลัง login/register สำเร็จ
public class AuthResponse {

    private String token; // JWT token ที่ client จะเก็บไว้ใช้
    private String email;
    private String role;
    private Long userId;

    public AuthResponse(String token, String email, String role, Long userId) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Long getUserId() {
        return userId;
    }
}
