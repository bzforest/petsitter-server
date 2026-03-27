package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.config.JwtHelper;
import com.company.pet_sitter_server.dto.sitter.*;
import com.company.pet_sitter_server.entity.SitterImage;
import com.company.pet_sitter_server.service.SitterProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sitters")
public class SitterController {

    @Autowired private SitterProfileService sitterProfileService;
    @Autowired private JwtHelper jwtHelper;

    // ─── Public endpoints ────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<Page<SitterProfileResponse>> searchSitters(
            @RequestParam(required = false) String petType,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer minExp,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                sitterProfileService.searchSitters(petType, minRating, minExp, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SitterProfileResponse> getSitterById(@PathVariable Long id) {
        return ResponseEntity.ok(sitterProfileService.getSitterById(id));
    }

    // ─── SITTER-only endpoints (enforced by SecurityConfig + @PreAuthorize) ──

    @GetMapping("/me")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<SitterProfileResponse> getMyProfile() {
        return ResponseEntity.ok(sitterProfileService.getMyProfile(jwtHelper.getCurrentUserId()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<SitterProfileResponse> updateMyProfile(
            @RequestBody SitterProfileRequest request) {
        return ResponseEntity.ok(
                sitterProfileService.updateMyProfile(jwtHelper.getCurrentUserId(), request));
    }

    @PostMapping("/me/request-approval")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<SitterProfileResponse> requestApproval() {
        return ResponseEntity.ok(sitterProfileService.requestApproval(jwtHelper.getCurrentUserId()));
    }

    @PostMapping("/me/images")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<SitterImage> addImage(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                sitterProfileService.addImage(jwtHelper.getCurrentUserId(), body.get("imageUrl")));
    }

    @DeleteMapping("/me/images/{imageId}")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        sitterProfileService.deleteImage(jwtHelper.getCurrentUserId(), imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/services")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<List<SitterOfferResponse>> getMyServices() {
        return ResponseEntity.ok(
                sitterProfileService.getMyProfile(jwtHelper.getCurrentUserId()).getServices());
    }

    @PostMapping("/me/services")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<SitterOfferResponse> addService(@RequestBody SitterOfferRequest request) {
        return ResponseEntity.ok(sitterProfileService.addOffer(jwtHelper.getCurrentUserId(), request));
    }

    @PutMapping("/me/services/{offerId}")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<SitterOfferResponse> updateService(@PathVariable Long offerId,
                                                              @RequestBody SitterOfferRequest request) {
        return ResponseEntity.ok(
                sitterProfileService.updateOffer(jwtHelper.getCurrentUserId(), offerId, request));
    }

    @DeleteMapping("/me/services/{offerId}")
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<Void> deleteService(@PathVariable Long offerId) {
        sitterProfileService.deleteOffer(jwtHelper.getCurrentUserId(), offerId);
        return ResponseEntity.noContent().build();
    }
}
