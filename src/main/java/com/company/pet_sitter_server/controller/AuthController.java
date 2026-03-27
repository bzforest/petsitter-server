package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.dto.auth.AuthResponse;
import com.company.pet_sitter_server.dto.auth.LoginRequest;
import com.company.pet_sitter_server.dto.auth.RegisterRequest;
import com.company.pet_sitter_server.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/register/sitter")
    public ResponseEntity<AuthResponse> registerAsSitter(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerAsSitter(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}
