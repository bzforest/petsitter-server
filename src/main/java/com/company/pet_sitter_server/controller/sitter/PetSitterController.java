package com.company.pet_sitter_server.controller.sitter;

import com.company.pet_sitter_server.dto.sitter.PetSitterRequest;
import com.company.pet_sitter_server.dto.sitter.PetSitterResponse;
import com.company.pet_sitter_server.service.sitter.PetSitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sitter-services")
@RequiredArgsConstructor
public class PetSitterController {

    private final PetSitterService petSitterService;

    @GetMapping("/filter/{serviceId}")
    public ResponseEntity<List<PetSitterResponse>> getByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(petSitterService.getSittersByService(serviceId));
    }

    @PostMapping
    public ResponseEntity<PetSitterResponse> createSitterService(@RequestBody PetSitterRequest request) {
        return ResponseEntity.ok(petSitterService.createSitterService(request));
    }

    @GetMapping
    public ResponseEntity<List<PetSitterResponse>> getAllSitterServices() {
        return ResponseEntity.ok(petSitterService.getAllSitterServices());
    }
}
