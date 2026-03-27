package com.company.pet_sitter_server.user.controller;

import com.company.pet_sitter_server.user.dto.SitterProfileRequest;
import com.company.pet_sitter_server.user.dto.SitterProfileResponse;
import com.company.pet_sitter_server.user.service.SitterProfileService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/sitter-profiles")
public class SitterProfileController {

    private final SitterProfileService service;

    public SitterProfileController(SitterProfileService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<SitterProfileResponse> create(@RequestBody SitterProfileRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    // 🔥 PAGINATION + FILTER + SORT
    @GetMapping
    public ResponseEntity<Page<SitterProfileResponse>> getAll(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "pricePerHour") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(
                service.getAll(minPrice, maxPrice, page, size, sortBy, direction)
        );
    }
}