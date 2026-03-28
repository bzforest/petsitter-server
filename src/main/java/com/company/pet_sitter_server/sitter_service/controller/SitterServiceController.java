package com.company.pet_sitter_server.sitter_service.controller;

import com.company.pet_sitter_server.sitter_service.dto.SitterServiceRequest;
import com.company.pet_sitter_server.sitter_service.dto.SitterServiceResponse;
import com.company.pet_sitter_server.sitter_service.service.SitterServiceService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sitter-services")
@RequiredArgsConstructor
public class SitterServiceController {

    private final SitterServiceService petSitterService;

    @GetMapping("/filter/{serviceId}")
    public ResponseEntity<List<SitterServiceResponse>> getByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(petSitterService.getSittersByService(serviceId));
    }

    @PostMapping
    public ResponseEntity<SitterServiceResponse> createSitterService(@RequestBody SitterServiceRequest request) {
        return ResponseEntity.ok(petSitterService.createSitterService(request));
    }

    @GetMapping
    public ResponseEntity<List<SitterServiceResponse>> getAllSitterServices() {
        return ResponseEntity.ok(petSitterService.getAllSitterServices());
    }
}
