package com.company.pet_sitter_server.auth.controller;

import com.company.pet_sitter_server.auth.dto.AuthResponse;
import com.company.pet_sitter_server.auth.dto.GoogleOAuthRequest;
import com.company.pet_sitter_server.auth.dto.LoginRequest;
import com.company.pet_sitter_server.auth.dto.RegisterRequest;
import com.company.pet_sitter_server.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/register
     * Body: { "email": "...", "password": "...", "role": "USER" }
     * Response: { "token": "...", "email": "...", "role": "USER", "userId": 1 }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     * Body: { "email": "...", "password": "..." }
     * Response: { "token": "...", "email": "...", "role": "USER", "userId": 1 }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/google
     * Body: { "accessToken": "<supabase_access_token_from_google_oauth>" }
     * Response: { "token": "...", "email": "...", "role": "USER", "userId": 1 }
     *
     * Flow:
     * 1. Frontend ทำ Google OAuth ผ่าน Supabase → ได้ Supabase session
     * 2. Frontend ส่ง session.access_token มาที่ endpoint นี้
     * 3. Backend verify กับ Supabase และออก JWT ของเรา
     */
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleOAuthRequest request) {
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(response);
    }
}
