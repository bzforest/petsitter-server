package com.company.pet_sitter_server.service_type.controller;

import com.company.pet_sitter_server.service_type.dto.ServiceTypeResponse;
import com.company.pet_sitter_server.service_type.service.ServiceTypeService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceTypeController {
    private final ServiceTypeService serviceTypeService;

    @GetMapping
    public ResponseEntity<List<ServiceTypeResponse>> getAllServices() {
        return ResponseEntity.ok(serviceTypeService.getAllServiceTypes());
    }
}