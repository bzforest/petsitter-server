package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.dto.PetSitterRequest;
import com.company.pet_sitter_server.dto.PetSitterResponse;
import com.company.pet_sitter_server.entity.PetSitter;
import com.company.pet_sitter_server.repository.PetSitterRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PetSitterService {
    @Autowired
    private PetSitterRepository repository;

    public PetSitterResponse createSitter(PetSitterRequest request) {
        // 1. แปลง Request DTO -> Entity
        PetSitter sitter = new PetSitter();
        sitter.setFullName(request.getFullName());
        sitter.setExperience(request.getExperience());
        sitter.setPricePerHour(request.getPricePerHour());

        // 2. บันทึกลง DB
        PetSitter savedSitter = repository.save(sitter);

        // 3. แปลง Entity -> Response DTO เพื่อส่งกลับ
        PetSitterResponse response = new PetSitterResponse();
        response.setId(savedSitter.getId());
        response.setFullName(savedSitter.getFullName());
        response.setExperience(savedSitter.getExperience());
        response.setPricePerHour(savedSitter.getPricePerHour());

        return response;
    }

    public List<PetSitter> getAllSitters() {
        return repository.findAll();
    }

    public PetSitterResponse updateSitter(UUID id, PetSitterRequest request) {
        // 1. ค้นหาข้อมูลเดิม
        PetSitter existingSitter = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sitter not found with id: " + id));

        // 2. อัปเดตข้อมูลใหม่ทับของเก่า
        existingSitter.setFullName(request.getFullName());
        existingSitter.setExperience(request.getExperience());
        existingSitter.setPricePerHour(request.getPricePerHour());

        // 3. บันทึก (Save ทับของเดิม)
        PetSitter updatedSitter = repository.save(existingSitter);

        // 4. เตรียม Response
        PetSitterResponse response = new PetSitterResponse();
        response.setId(updatedSitter.getId());
        response.setFullName(updatedSitter.getFullName());
        response.setExperience(updatedSitter.getExperience());
        response.setPricePerHour(updatedSitter.getPricePerHour());

        return response;
    }

    public void deleteSitter(UUID id) {
        repository.deleteById(id);
    }
}