package com.company.pet_sitter_server.pets.controller;

import com.company.pet_sitter_server.pets.dto.PetRequest;
import com.company.pet_sitter_server.pets.dto.PetResponse;
import com.company.pet_sitter_server.pets.entity.Pet;
import com.company.pet_sitter_server.pets.service.PetService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<PetResponse> createPet(
            @ModelAttribute PetRequest request, // เปลี่ยนจาก @RequestBody เป็น @ModelAttribute
            @RequestParam(value = "image", required = false) MultipartFile image) {
        Pet savedPet = petService.createPet(request, image); // ส่ง image ไปที่ Service ด้วย
        PetResponse response = petService.convertToResponse(savedPet);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> updatePet(
            @PathVariable Long id, 
            @ModelAttribute PetRequest request, // เปลี่ยนตรงนี้ด้วย
            @RequestParam(value = "image", required = false) MultipartFile image) {
        PetResponse response = petService.updatePet(id, request, image); // ส่ง image ไปที่ Service ด้วย
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return ResponseEntity.ok("Pet deleted successfully");
    }
}