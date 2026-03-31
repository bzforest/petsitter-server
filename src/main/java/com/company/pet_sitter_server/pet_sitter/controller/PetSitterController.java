package com.company.pet_sitter_server.pet_sitter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.company.pet_sitter_server.pet_sitter.dto.PetSitterRequest;
import com.company.pet_sitter_server.pet_sitter.dto.PetSitterResponse;
import com.company.pet_sitter_server.pet_sitter.entity.PetSitter;
import com.company.pet_sitter_server.pet_sitter.service.PetSitterService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sitters")
public class PetSitterController {

    @Autowired
    private PetSitterService service;

    @GetMapping("/profile")
    public ResponseEntity<PetSitterResponse> getProfile() {
        return ResponseEntity.ok(service.getProfile());
    }

    @GetMapping
    public ResponseEntity<List<PetSitter>> getAllSitters() {
        return ResponseEntity.ok(service.getAllSitters());
    }

    @PostMapping
    public ResponseEntity<PetSitterResponse> createSitter(@RequestBody PetSitterRequest request) {
        return ResponseEntity.ok(service.createSitter(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetSitterResponse> updateSitter(@PathVariable UUID id, @RequestBody PetSitterRequest request) {
        return ResponseEntity.ok(service.updateSitter(id, request));
    }

    @PatchMapping("/{id}/request-approval")
    public ResponseEntity<PetSitterResponse> requestApproval(@PathVariable UUID id) {
        return ResponseEntity.ok(service.requestApproval(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSitter(@PathVariable UUID id) {
        service.deleteSitter(id);
        return ResponseEntity.noContent().build();
    }
}
