package com.company.pet_sitter_server.user.service;

import com.company.pet_sitter_server.enums.Role;
import com.company.pet_sitter_server.enums.UserStatus;
import com.company.pet_sitter_server.user.dto.UserRequest;
import com.company.pet_sitter_server.user.dto.UserResponse;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    // ✅ CREATE + VALIDATION
    public UserResponse create(UserRequest req) {

        // 🔥 email ซ้ำ
        if (repository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 🔥 role ต้องถูก
        Role role;
        try {
            role = Role.valueOf(req.getRole().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid role");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        User saved = repository.save(user);
        return mapToResponse(saved);
    }

    public List<User> getAll() {
        return repository.findAll();
    }

    public User getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void banUser(Long id) {
        User user = getById(id);
        user.setStatus(UserStatus.BANNED);
        repository.save(user);
    }

    public void unbanUser(Long id) {
        User user = getById(id);
        user.setStatus(UserStatus.ACTIVE);
        repository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole().name());
        res.setStatus(user.getStatus().name());
        return res;
    }
}