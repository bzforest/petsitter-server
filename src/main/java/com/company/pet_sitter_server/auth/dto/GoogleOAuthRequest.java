package com.company.pet_sitter_server.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body สำหรับ Google OAuth login
 *
 * Frontend จะส่ง Supabase access_token มาให้ backend verify
 * token นี้ได้มาจาก supabase.auth.getSession() หลัง Google OAuth callback
 */
public class GoogleOAuthRequest {

    // Supabase access_token ที่ได้จาก Google OAuth flow
    @NotBlank(message = "Access token is required")
    private String accessToken;

    public GoogleOAuthRequest() {}

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}
