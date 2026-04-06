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
    private final OwnerStorageService storageService;

    public UserProfileService(UserProfileRepository profileRepo, UserRepository userRepo, OwnerStorageService storageService) {
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
        this.storageService = storageService;
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

    // ==========================================
    // (Owner Profile)
    // ==========================================
    public UserProfile getProfileByEmail(String email) {
        return profileRepo.findByUser_Email(email)
                .orElseGet(() -> {
                    // 🟢 ถ้าหาไม่เจอ ให้สร้างแถวใหม่ใน DB ให้เลยทันที
                    User user = userRepo.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    return profileRepo.save(newProfile); 
                });
    }

    @org.springframework.transaction.annotation.Transactional
    public UserProfile updateMyProfile(
            String email, String fullName, String phone, 
            String idNumber, String dob, 
            org.springframework.web.multipart.MultipartFile image) {
        
        UserProfile profile = getProfileByEmail(email);

        profile.setFullName(fullName);
        profile.setPhone(phone);
        profile.setIdNumber(idNumber);
        
        if (dob != null && !dob.isBlank()) {
            profile.setDateOfBirth(java.time.LocalDate.parse(dob));
        }

        // 🟢 3. จัดการอัปโหลดรูปภาพลงถังใหม่ "owner-profiles"
        if (image != null && !image.isEmpty()) {
            String imageUrl = storageService.uploadImage(image, "owner-profiles");
            profile.setProfileImage(imageUrl);
        }

        return profileRepo.save(profile);
    }
}
