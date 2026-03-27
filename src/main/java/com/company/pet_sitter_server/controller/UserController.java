package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.config.JwtHelper;
import com.company.pet_sitter_server.dto.user.ChangePasswordRequest;
import com.company.pet_sitter_server.dto.user.UserProfileRequest;
import com.company.pet_sitter_server.dto.user.UserProfileResponse;
import com.company.pet_sitter_server.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private JwtHelper jwtHelper;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile(jwtHelper.getCurrentUserId()));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(jwtHelper.getCurrentUserId(), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(jwtHelper.getCurrentUserId(), request);
        return ResponseEntity.noContent().build();
    }
}
