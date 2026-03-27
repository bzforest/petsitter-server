package com.company.pet_sitter_server.user.service;

import com.company.pet_sitter_server.user.dto.UserProfileRequest;
import com.company.pet_sitter_server.user.dto.UserProfileResponse;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.entity.UserProfile;
import com.company.pet_sitter_server.user.repository.UserProfileRepository;
import com.company.pet_sitter_server.user.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserProfileRepository profileRepo;
    private final UserRepository userRepo;

    public UserProfileService(UserProfileRepository profileRepo, UserRepository userRepo) {
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
    }

    public UserProfileResponse create(UserProfileRequest req) {
        User user = userRepo.findById(req.userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = new UserProfile();
        profile.setFullName(req.fullName);
        profile.setPhone(req.phone);
        profile.setAddress(req.address);
        profile.setUser(user);

        profileRepo.save(profile);

        UserProfileResponse res = new UserProfileResponse();
        res.id = profile.getId();
        res.fullName = profile.getFullName();
        res.phone = profile.getPhone();
        res.address = profile.getAddress();
        res.userId = user.getId();

        return res;
    }
}
