package com.company.pet_sitter_server.service.service_type;

import com.company.pet_sitter_server.dto.service_type.ServiceTypeResponse; // Import DTO ที่สร้างใหม่
import com.company.pet_sitter_server.entity.service_type.ServiceType;
import com.company.pet_sitter_server.repository.service_type.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;

    public List<ServiceTypeResponse> getAllServiceTypes() {
        return serviceTypeRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ServiceTypeResponse getServiceTypeById(Long id) {
        return serviceTypeRepository.findById(id)
                .map(this::convertToResponse)
                .orElse(null);
    }

    private ServiceTypeResponse convertToResponse(ServiceType entity) {
        ServiceTypeResponse dto = new ServiceTypeResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}