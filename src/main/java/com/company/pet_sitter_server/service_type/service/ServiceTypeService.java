package com.company.pet_sitter_server.service_type.service;

import com.company.pet_sitter_server.service_type.dto.ServiceTypeResponse;
import com.company.pet_sitter_server.service_type.entity.ServiceType;
import com.company.pet_sitter_server.service_type.repository.ServiceTypeRepository;

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