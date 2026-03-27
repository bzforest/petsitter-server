package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.config.JwtHelper;
import com.company.pet_sitter_server.dto.upload.FileUploadResponse;
import com.company.pet_sitter_server.service.SupabaseStorageService;
import com.company.pet_sitter_server.service.UserService;
import com.company.pet_sitter_server.service.PetService;
import com.company.pet_sitter_server.service.SitterProfileService;
import com.company.pet_sitter_server.dto.user.UserProfileRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles all file uploads to Supabase Storage.
 *
 * Usage from frontend:
 *   POST /api/upload/profile    — profile image
 *   POST /api/upload/pet/{id}   — pet image
 *   POST /api/upload/sitter     — sitter gallery image
 *   POST /api/upload/payment    — payment slip
 *
 * All endpoints accept: multipart/form-data  with field name "file"
 * Returns: { "url": "https://...supabase.co/...", "filename": "...", ... }
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired private SupabaseStorageService storageService;
    @Autowired private JwtHelper jwtHelper;
    @Autowired private UserService userService;
    @Autowired private PetService petService;
    @Autowired private SitterProfileService sitterProfileService;

    /**
     * Upload and set the current user's profile image.
     * Updates UserProfile.profileImage automatically.
     */
    @PostMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {

        Long userId = jwtHelper.getCurrentUserId();
        String folder = "profiles/" + userId;
        FileUploadResponse result = storageService.upload(file, folder);

        UserProfileRequest req = new UserProfileRequest();
        req.setProfileImage(result.getUrl());
        userService.updateProfileImageOnly(userId, result.getUrl());

        return ResponseEntity.ok(result);
    }

    /**
     * Upload and set a pet's image.
     * Validates that the pet belongs to the current user.
     */
    @PostMapping(value = "/pet/{petId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadPetImage(
            @PathVariable Long petId,
            @RequestParam("file") MultipartFile file) {

        Long userId = jwtHelper.getCurrentUserId();
        String folder = "pets/" + userId + "/" + petId;
        FileUploadResponse result = storageService.upload(file, folder);

        petService.updatePetImageOnly(petId, userId, result.getUrl());
        return ResponseEntity.ok(result);
    }

    /**
     * Upload a sitter gallery image.
     * Adds to sitter_images table and returns public URL.
     */
    @PostMapping(value = "/sitter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<FileUploadResponse> uploadSitterImage(
            @RequestParam("file") MultipartFile file) {

        Long userId = jwtHelper.getCurrentUserId();
        String folder = "sitters/" + userId;
        FileUploadResponse result = storageService.upload(file, folder);

        sitterProfileService.addImage(userId, result.getUrl());
        return ResponseEntity.ok(result);
    }

    /**
     * Upload a payment slip image.
     * Returns the public URL only — caller saves it with the payment.
     */
    @PostMapping(value = "/payment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadPaymentSlip(
            @RequestParam("file") MultipartFile file) {

        Long userId = jwtHelper.getCurrentUserId();
        String folder = "payments/" + userId;
        return ResponseEntity.ok(storageService.upload(file, folder));
    }

    /**
     * Upload a sitter payout book-bank image.
     */
    @PostMapping(value = "/payout", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<FileUploadResponse> uploadBookBankImage(
            @RequestParam("file") MultipartFile file) {

        Long userId = jwtHelper.getCurrentUserId();
        String folder = "payout/" + userId;
        return ResponseEntity.ok(storageService.upload(file, folder));
    }
}
