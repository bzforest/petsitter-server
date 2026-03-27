package com.company.pet_sitter_server.address.controller;

import com.company.pet_sitter_server.address.dto.AddressRequest;
import com.company.pet_sitter_server.address.dto.AddressResponse;
import com.company.pet_sitter_server.address.service.AddressService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService service;

    public AddressController(AddressService service) {
        this.service = service;
    }

    // ✅ CREATE
    @PostMapping
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody AddressRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    // ✅ GET BY USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getByUser(userId));
    }

    // ✅ GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest req
    ) {
        return ResponseEntity.ok(service.update(id, req));
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}