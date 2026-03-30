package com.company.pet_sitter_server.sitter_service.service;

import com.company.pet_sitter_server.service_type.entity.ServiceType;
import com.company.pet_sitter_server.service_type.repository.ServiceTypeRepository;
import com.company.pet_sitter_server.sitter_service.dto.SitterServiceRequest;
import com.company.pet_sitter_server.sitter_service.dto.SitterServiceResponse;
import com.company.pet_sitter_server.sitter_service.entity.SitterService;
import com.company.pet_sitter_server.sitter_service.repository.SitterServiceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SitterServiceService {

    private final SitterServiceRepository petSitterRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    // 1. บันทึกข้อมูลบริการของพี่เลี้ยง
    public SitterServiceResponse createSitterService(SitterServiceRequest request) {
        ServiceType serviceType = serviceTypeRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service Type not found with ID: " + request.getServiceId()));

        SitterService sitter = new SitterService();
        sitter.setSitterId(request.getSitterId());
        sitter.setServiceType(serviceType);
        sitter.setPricePerHour(request.getPricePerHour());
        sitter.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);

        SitterService saved = petSitterRepository.save(sitter);
        return convertToResponse(saved);
    }

    // 2. ดึงรายการบริการทั้งหมด (เพื่อตรวจสอบ)
    public List<SitterServiceResponse> getAllSitterServices() {
        return petSitterRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 3. ดึงรายการพี่เลี้ยงตามประเภทบริการ
    public List<SitterServiceResponse> getSittersByService(Long serviceId) {
        return petSitterRepository.findByServiceTypeId(serviceId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Helper Method แปลง Entity เป็น Response DTO
    private SitterServiceResponse convertToResponse(SitterService entity) {
        SitterServiceResponse response = new SitterServiceResponse();
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