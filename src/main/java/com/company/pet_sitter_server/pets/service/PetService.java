package com.company.pet_sitter_server.pets.service;

import com.company.pet_sitter_server.pets.dto.PetRequest;
import com.company.pet_sitter_server.pets.dto.PetResponse;
import com.company.pet_sitter_server.pets.entity.Pet;
import com.company.pet_sitter_server.pets.repository.PetRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    public Pet getPetById(Long id) {
        return petRepository.findById(id).orElse(null);
    }

    // สำหรับดึงข้อมูล
    public List<PetResponse> getPetsByUserId(Long userId) {
        return petRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse) // ใช้ Method เดิมที่เราเขียนไว้แปลงทีละตัว
                .collect(Collectors.toList());
    }

    // สำหรับ Create ข้อมูล
    public Pet createPet(PetRequest request) {
        Pet pet = new Pet();
        pet.setName(request.getName());
        pet.setType(request.getType());
        pet.setBreed(request.getBreed());
        pet.setSex(request.getSex());
        pet.setAge(request.getAge());
        pet.setWeight(request.getWeight());
        pet.setAboutPet(request.getAboutPet());
        pet.setImageUrl(request.getImageUrl());
        pet.setUserId(request.getUserId());
        return petRepository.save(pet);
    }

    // สำหรับ Update ข้อมูล
    public PetResponse updatePet(Long id, PetRequest request) {
        // หา Pet เดิมจาก DB ถ้าไม่เจอให้โยน Error (หรือจัดการตามความเหมาะสม)
        Pet existingPet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));

        // อัปเดตค่าใหม่จาก Request (ถุงรับของ) ลงใน Entity
        existingPet.setName(request.getName());
        existingPet.setType(request.getType());
        existingPet.setBreed(request.getBreed());
        existingPet.setSex(request.getSex());
        existingPet.setAge(request.getAge());
        existingPet.setWeight(request.getWeight());
        existingPet.setAboutPet(request.getAboutPet());
        existingPet.setImageUrl(request.getImageUrl());
        // ปกติ userId จะไม่เปลี่ยน แต่ถ้าต้องการให้เปลี่ยนได้ก็ใส่เพิ่มครับ

        Pet updatedPet = petRepository.save(existingPet); // บันทึกทับตัวเดิม
        return convertToResponse(updatedPet);
    }

    // สำหรับ Delete ข้อมูล
    public void deletePet(Long id) {
        if (!petRepository.existsById(id)) {
            throw new RuntimeException("Pet not found with id: " + id);
        }
        petRepository.deleteById(id);
    }

    public PetResponse convertToResponse(Pet pet) {
        PetResponse response = new PetResponse();
        response.setId(pet.getId());
        response.setName(pet.getName());
        response.setType(pet.getType());
        response.setBreed(pet.getBreed());
        response.setSex(pet.getSex());
        response.setAge(pet.getAge());
        response.setWeight(pet.getWeight());
        response.setAboutPet(pet.getAboutPet());
        response.setImageUrl(pet.getImageUrl());
        response.setUserId(pet.getUserId());
        response.setCreatedAt(pet.getCreatedAt());
        return response;
    }
}
