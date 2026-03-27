package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.config.JwtUtil;
import com.company.pet_sitter_server.dto.auth.AuthResponse;
import com.company.pet_sitter_server.dto.auth.LoginRequest;
import com.company.pet_sitter_server.dto.auth.RegisterRequest;
import com.company.pet_sitter_server.entity.User;
import com.company.pet_sitter_server.entity.UserProfile;
import com.company.pet_sitter_server.enums.UserRole;
import com.company.pet_sitter_server.exception.BadRequestException;
import com.company.pet_sitter_server.exception.UnauthorizedException;
import com.company.pet_sitter_server.repository.UserProfileRepository;
import com.company.pet_sitter_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        return doRegister(request, UserRole.USER);
    }

    @Transactional
    public AuthResponse registerAsSitter(RegisterRequest request) {
        return doRegister(request, UserRole.SITTER);
    }

    private AuthResponse doRegister(RegisterRequest request, UserRole role) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        User saved = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(saved.getId());
        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        userProfileRepository.save(profile);

        return buildAuthResponse(saved, request.getFullName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Your account has been banned");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String fullName = userProfileRepository.findByUserId(user.getId())
                .map(UserProfile::getFullName)
                .orElse("");

        return buildAuthResponse(user, fullName);
    }

    private AuthResponse buildAuthResponse(User user, String fullName) {
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        AuthResponse r = new AuthResponse();
        r.setToken(token);
        r.setUserId(user.getId());
        r.setEmail(user.getEmail());
        r.setRole(user.getRole().name());
        r.setFullName(fullName);
        return r;
    }
}
