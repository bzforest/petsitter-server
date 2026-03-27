package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.config.JwtHelper;
import com.company.pet_sitter_server.dto.pet.PetRequest;
import com.company.pet_sitter_server.dto.pet.PetResponse;
import com.company.pet_sitter_server.service.PetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    @Autowired private PetService petService;
    @Autowired private JwtHelper jwtHelper;

    @GetMapping
    public ResponseEntity<List<PetResponse>> getMyPets() {
        return ResponseEntity.ok(petService.getMyPets(jwtHelper.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        return ResponseEntity.ok(petService.getPetById(id, jwtHelper.getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<PetResponse> createPet(@Valid @RequestBody PetRequest request) {
        return ResponseEntity.ok(petService.createPet(jwtHelper.getCurrentUserId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> updatePet(@PathVariable Long id,
                                                  @Valid @RequestBody PetRequest request) {
        return ResponseEntity.ok(petService.updatePet(id, jwtHelper.getCurrentUserId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        petService.deletePet(id, jwtHelper.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
