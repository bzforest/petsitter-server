package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.dto.user.ChangePasswordRequest;
import com.company.pet_sitter_server.dto.user.UserProfileRequest;
import com.company.pet_sitter_server.dto.user.UserProfileResponse;
import com.company.pet_sitter_server.entity.User;
import com.company.pet_sitter_server.entity.UserProfile;
import com.company.pet_sitter_server.repository.UserProfileRepository;
import com.company.pet_sitter_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(new UserProfile());

        return toResponse(user, profile);
    }

    public UserProfileResponse updateMyProfile(Long userId, UserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUserId(userId);
                    return p;
                });

        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile.setProfileImage(request.getProfileImage());
        profile.setIdNumber(request.getIdNumber());
        profile.setDateOfBirth(request.getDateOfBirth());
        userProfileRepository.save(profile);

        return toResponse(user, profile);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void updateProfileImageOnly(Long userId, String imageUrl) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUserId(userId);
                    return p;
                });
        profile.setProfileImage(imageUrl);
        userProfileRepository.save(profile);
    }

    private UserProfileResponse toResponse(User user, UserProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setFullName(profile.getFullName());
        response.setPhone(profile.getPhone());
        response.setProfileImage(profile.getProfileImage());
        response.setIdNumber(profile.getIdNumber());
        response.setDateOfBirth(profile.getDateOfBirth());
        return response;
    }
}
