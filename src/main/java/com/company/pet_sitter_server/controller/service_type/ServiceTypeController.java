package com.company.pet_sitter_server.controller.service_type;

import com.company.pet_sitter_server.dto.service_type.ServiceTypeResponse;
import com.company.pet_sitter_server.service.service_type.ServiceTypeService;
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