package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.service.PetSitterService;
import com.company.pet_sitter_server.dto.PetSitterResponse;
import com.company.pet_sitter_server.entity.PetSitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.company.pet_sitter_server.dto.PetSitterRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sitters")
public class PetSitterController {

    @Autowired
    private PetSitterService service;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSitter(@PathVariable UUID id) {
        service.deleteSitter(id);
        return ResponseEntity.noContent().build();
    }
}