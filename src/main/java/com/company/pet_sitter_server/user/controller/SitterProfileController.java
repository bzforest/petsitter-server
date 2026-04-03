package com.company.pet_sitter_server.user.controller;

import com.company.pet_sitter_server.user.dto.SitterProfileRequest;
import com.company.pet_sitter_server.user.dto.SitterProfileResponse;
import com.company.pet_sitter_server.user.dto.SitterProfileUpdateRequest;
import com.company.pet_sitter_server.user.service.SitterProfileService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sitter-profiles")
public class SitterProfileController {

    private final SitterProfileService service;

    public SitterProfileController(SitterProfileService service) {
        this.service = service;
    }

    // GET profile ของ user ที่ login อยู่
    @GetMapping("/me")
    public ResponseEntity<SitterProfileResponse> getMe(Authentication auth) {
        return ResponseEntity.ok(service.getMe(auth.getName()));
    }

    // UPDATE profile
    @PutMapping("/{id}")
    public ResponseEntity<SitterProfileResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SitterProfileUpdateRequest req
    ) {
        return ResponseEntity.ok(service.update(id, req));
    }

    // REQUEST APPROVAL
    @PatchMapping("/{id}/request-approval")
    public ResponseEntity<SitterProfileResponse> requestApproval(@PathVariable Long id) {
        return ResponseEntity.ok(service.requestApproval(id));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<SitterProfileResponse> create(@Valid @RequestBody SitterProfileRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    // PAGINATION + FILTER + SORT
    @GetMapping
    public ResponseEntity<Page<SitterProfileResponse>> getAll(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) java.util.List<String> petTypes,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String experience,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(
                service.getAll(minPrice, maxPrice, query, petTypes, rating, experience, page, size, sortBy, direction)
        );
    }

    // GET By ID
    @GetMapping("/{id}")
    public ResponseEntity<SitterProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}