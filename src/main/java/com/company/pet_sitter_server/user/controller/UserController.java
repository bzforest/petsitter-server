package com.company.pet_sitter_server.user.controller;

import com.company.pet_sitter_server.user.dto.UserRequest;
import com.company.pet_sitter_server.user.dto.UserResponse;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PatchMapping("/{id}/ban")
    public ResponseEntity<Void> ban(@PathVariable Long id) {
        service.banUser(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/unban")
    public ResponseEntity<Void> unban(@PathVariable Long id) {
        service.unbanUser(id);
        return ResponseEntity.ok().build();
    }
}
