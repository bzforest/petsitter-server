package com.company.pet_sitter_server.user.controller;

import com.company.pet_sitter_server.user.dto.UserProfileRequest;
import com.company.pet_sitter_server.user.dto.UserProfilePublicResponse;
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

    // ==========================================
    // (Owner Profile)
    // ==========================================

    // 1. ดึงข้อมูลโปรไฟล์ของตัวเอง
    @GetMapping("/me")
    public ResponseEntity<com.company.pet_sitter_server.user.entity.UserProfile> getMyProfile() {
        // ดึง Email ของคนที่ Login อยู่
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(service.getProfileByEmail(email));
    }

    // 1.1 ดึง owner profile จาก user id (ใช้ใน sitter booking detail: View Profile)
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfileResponse> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    // 2. อัปเดตข้อมูลโปรไฟล์
    @PutMapping(value = "/me", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<com.company.pet_sitter_server.user.entity.UserProfile> updateMyProfile(
            @RequestParam("full_name") String fullName,
            @RequestParam("phone") String phone,
            @RequestParam(value = "id_number", required = false) String idNumber,
            @RequestParam(value = "date_of_birth", required = false) String dob,
            @RequestParam(value = "image", required = false) org.springframework.web.multipart.MultipartFile image
    ) {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        
        return ResponseEntity.ok(service.updateMyProfile(email, fullName, phone, idNumber, dob, image));
    }
}
