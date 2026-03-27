package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.dto.pet.PetRequest;
import com.company.pet_sitter_server.dto.pet.PetResponse;
import com.company.pet_sitter_server.entity.Pet;
import com.company.pet_sitter_server.exception.AccessDeniedException;
import com.company.pet_sitter_server.exception.NotFoundException;
import com.company.pet_sitter_server.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    public List<PetResponse> getMyPets(Long userId) {
        return petRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public PetResponse getPetById(Long petId, Long userId) {
        Pet pet = findAndVerifyOwner(petId, userId);
        return toResponse(pet);
    }

    public PetResponse createPet(Long userId, PetRequest request) {
        Pet pet = new Pet();
        pet.setUserId(userId);
        applyRequest(pet, request);
        return toResponse(petRepository.save(pet));
    }

    public PetResponse updatePet(Long petId, Long userId, PetRequest request) {
        Pet pet = findAndVerifyOwner(petId, userId);
        applyRequest(pet, request);
        return toResponse(petRepository.save(pet));
    }

    public void deletePet(Long petId, Long userId) {
        findAndVerifyOwner(petId, userId);
        petRepository.deleteById(petId);
    }

    public void updatePetImageOnly(Long petId, Long userId, String imageUrl) {
        Pet pet = findAndVerifyOwner(petId, userId);
        pet.setImageUrl(imageUrl);
        petRepository.save(pet);
    }

    private Pet findAndVerifyOwner(Long petId, Long userId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException("Pet not found with id: " + petId));
        if (!pet.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not own this pet");
        }
        return pet;
    }

    private void applyRequest(Pet pet, PetRequest req) {
        pet.setName(req.getName());
        pet.setType(req.getType());
        pet.setBreed(req.getBreed());
        pet.setSex(req.getSex());
        pet.setAge(req.getAge());
        pet.setWeight(req.getWeight());
        pet.setColor(req.getColor());
        pet.setAboutPet(req.getAboutPet());
        pet.setImageUrl(req.getImageUrl());
    }

    public PetResponse toResponse(Pet pet) {
        PetResponse r = new PetResponse();
        r.setId(pet.getId());
        r.setUserId(pet.getUserId());
        r.setName(pet.getName());
        r.setType(pet.getType());
        r.setBreed(pet.getBreed());
        r.setSex(pet.getSex());
        r.setAge(pet.getAge());
        r.setWeight(pet.getWeight());
        r.setColor(pet.getColor());
        r.setAboutPet(pet.getAboutPet());
        r.setImageUrl(pet.getImageUrl());
        return r;
    }
}
