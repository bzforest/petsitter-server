package com.company.pet_sitter_server.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body สำหรับ Google OAuth login
 *
 * Frontend จะส่ง Supabase access_token มาให้ backend verify
 * token นี้ได้มาจาก supabase.auth.getSession() หลัง Google OAuth callback
 */
public class GoogleOAuthRequest {

    @NotBlank(message = "Access token is required")
    private String accessToken;

    /**
     * Role ที่ user เลือกจาก GoogleRoleModal ก่อน redirect ไป Google
     * - สำหรับ user ใหม่ → ใช้ set role ตอนสร้าง account
     * - สำหรับ user เก่า → ใช้ตรวจว่าตรงกับ role ใน DB มั้ย
     * - ถ้าไม่ส่งมา หรือ null → default = "USER"
     */
    private String intendedRole;

    public GoogleOAuthRequest() {}

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getIntendedRole() { return intendedRole; }
    public void setIntendedRole(String intendedRole) { this.intendedRole = intendedRole; }
}
