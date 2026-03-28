package com.company.pet_sitter_server.service.sitter;

import com.company.pet_sitter_server.dto.sitter.PetSitterRequest;
import com.company.pet_sitter_server.dto.sitter.PetSitterResponse;
import com.company.pet_sitter_server.entity.sitter.PetSitter;
import com.company.pet_sitter_server.repository.sitter.PetSitterRepository;
import com.company.pet_sitter_server.entity.service_type.ServiceType;
import com.company.pet_sitter_server.repository.service_type.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetSitterService {

    private final PetSitterRepository petSitterRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    // 1. บันทึกข้อมูลบริการของพี่เลี้ยง
    public PetSitterResponse createSitterService(PetSitterRequest request) {
        ServiceType serviceType = serviceTypeRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service Type not found with ID: " + request.getServiceId()));

        PetSitter sitter = new PetSitter();
        sitter.setSitterId(request.getSitterId());
        sitter.setServiceType(serviceType);
        sitter.setPricePerHour(request.getPricePerHour());
        sitter.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);

        PetSitter saved = petSitterRepository.save(sitter);
        return convertToResponse(saved);
    }

    // 2. ดึงรายการบริการทั้งหมด (เพื่อตรวจสอบ)
    public List<PetSitterResponse> getAllSitterServices() {
        return petSitterRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 3. ดึงรายการพี่เลี้ยงตามประเภทบริการ
    public List<PetSitterResponse> getSittersByService(Long serviceId) {
        return petSitterRepository.findByServiceTypeId(serviceId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Helper Method แปลง Entity เป็น Response DTO
    private PetSitterResponse convertToResponse(PetSitter entity) {
        PetSitterResponse response = new PetSitterResponse();
        response.setId(entity.getId());
        response.setSitterId(entity.getSitterId());
        response.setPricePerHour(entity.getPricePerHour());
        response.setIsAvailable(entity.getIsAvailable());

        if (entity.getServiceType() != null) {
            response.setServiceId(entity.getServiceType().getId());
            response.setServiceName(entity.getServiceType().getName());
        }

        return response;
    }
}