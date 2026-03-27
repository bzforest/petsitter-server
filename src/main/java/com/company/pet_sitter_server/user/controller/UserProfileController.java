package com.company.pet_sitter_server.user.controller;

import com.company.pet_sitter_server.user.dto.UserProfileRequest;
import com.company.pet_sitter_server.user.dto.UserProfileResponse;
import com.company.pet_sitter_server.user.service.UserProfileService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-profiles")
public class UserProfileController {

    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserProfileResponse> create(@RequestBody UserProfileRequest req) {
        return ResponseEntity.ok(service.create(req));
    }
}
