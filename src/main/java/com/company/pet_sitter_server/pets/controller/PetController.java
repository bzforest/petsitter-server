package com.company.pet_sitter_server.pets.controller;

import com.company.pet_sitter_server.pets.dto.PetRequest;
import com.company.pet_sitter_server.pets.dto.PetResponse;
import com.company.pet_sitter_server.pets.entity.Pet;
import com.company.pet_sitter_server.pets.service.PetService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping
    public ResponseEntity<List<Pet>> getAllPets() {
        return ResponseEntity.ok(petService.getAllPets());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PetResponse>> getPets(@PathVariable Long userId) {
        return ResponseEntity.ok(petService.getPetsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<PetResponse> createPet(@RequestBody PetRequest request) {
        Pet savedPet = petService.createPet(request);
        PetResponse response = petService.convertToResponse(savedPet);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> updatePet(@PathVariable Long id, @RequestBody PetRequest request) {
        PetResponse response = petService.updatePet(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return ResponseEntity.ok("Pet deleted successfully");
    }
}