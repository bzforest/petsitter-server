package com.company.pet_sitter_server.pet_sitter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.company.pet_sitter_server.pet_sitter.dto.PetSitterRequest;
import com.company.pet_sitter_server.pet_sitter.dto.PetSitterResponse;
import com.company.pet_sitter_server.pet_sitter.entity.PetSitter;
import com.company.pet_sitter_server.pet_sitter.repository.PetSitterRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PetSitterService {

    @Autowired
    private PetSitterRepository repository;

    public PetSitterResponse getProfile() {
        PetSitter sitter = repository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    PetSitter blank = new PetSitter();
                    blank.setStatus("pending");
                    return repository.save(blank);
                });
        return toResponse(sitter);
    }

    public PetSitterResponse createSitter(PetSitterRequest request) {
        PetSitter sitter = new PetSitter();
        applyRequest(sitter, request);
        return toResponse(repository.save(sitter));
    }

    public List<PetSitter> getAllSitters() {
        return repository.findAll();
    }

    public PetSitterResponse updateSitter(UUID id, PetSitterRequest request) {
        PetSitter sitter = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sitter not found with id: " + id));
        applyRequest(sitter, request);
        return toResponse(repository.save(sitter));
    }

    public void deleteSitter(UUID id) {
        repository.deleteById(id);
    }

    public PetSitterResponse requestApproval(UUID id) {
        PetSitter sitter = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sitter not found with id: " + id));
        sitter.setStatus("pending");
        sitter.setRejectionMessage(null);
        return toResponse(repository.save(sitter));
    }

    private void applyRequest(PetSitter sitter, PetSitterRequest request) {
        if (request.getFullName() != null) sitter.setFullName(request.getFullName());
        if (request.getPhone() != null) sitter.setPhone(request.getPhone());
        if (request.getEmail() != null) sitter.setEmail(request.getEmail());
        if (request.getIntro() != null) sitter.setIntro(request.getIntro());
        if (request.getTradeName() != null) sitter.setTradeName(request.getTradeName());
        if (request.getPetTypes() != null) sitter.setPetTypes(request.getPetTypes());
        if (request.getServices() != null) sitter.setServices(request.getServices());
        if (request.getPlace() != null) sitter.setPlace(request.getPlace());
        if (request.getExperience() != null) sitter.setExperience(request.getExperience());
        if (request.getPricePerHour() != null) sitter.setPricePerHour(request.getPricePerHour());
        if (request.getAddress() != null) sitter.setAddress(request.getAddress());
        if (request.getDistrict() != null) sitter.setDistrict(request.getDistrict());
        if (request.getSubDistrict() != null) sitter.setSubDistrict(request.getSubDistrict());
        if (request.getProvince() != null) sitter.setProvince(request.getProvince());
        if (request.getPostCode() != null) sitter.setPostCode(request.getPostCode());
        if (request.getLatitude() != null) sitter.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) sitter.setLongitude(request.getLongitude());
        if (request.getProfileImage() != null) sitter.setProfileImage(request.getProfileImage());
        if (request.getGallery() != null) sitter.setGallery(new ArrayList<>(request.getGallery()));
    }

    private PetSitterResponse toResponse(PetSitter s) {
        PetSitterResponse res = new PetSitterResponse();
        res.setId(s.getId());
        res.setFullName(s.getFullName());
        res.setPhone(s.getPhone());
        res.setEmail(s.getEmail());
        res.setIntro(s.getIntro());
        res.setTradeName(s.getTradeName());
        res.setPetTypes(s.getPetTypes());
        res.setServices(s.getServices());
        res.setPlace(s.getPlace());
        res.setExperience(s.getExperience());
        res.setPricePerHour(s.getPricePerHour());
        res.setAddress(s.getAddress());
        res.setDistrict(s.getDistrict());
        res.setSubDistrict(s.getSubDistrict());
        res.setProvince(s.getProvince());
        res.setPostCode(s.getPostCode());
        res.setLatitude(s.getLatitude());
        res.setLongitude(s.getLongitude());
        res.setProfileImage(s.getProfileImage());
        res.setGallery(s.getGallery());
        res.setStatus(s.getStatus());
        res.setRejectionMessage(s.getRejectionMessage());
        return res;
    }
}
