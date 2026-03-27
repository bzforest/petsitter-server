package com.company.pet_sitter_server.user.service;

import com.company.pet_sitter_server.enums.Role;
import com.company.pet_sitter_server.user.dto.SitterProfileRequest;
import com.company.pet_sitter_server.user.dto.SitterProfileResponse;
import com.company.pet_sitter_server.user.entity.SitterProfile;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.repository.SitterProfileRepository;
import com.company.pet_sitter_server.user.repository.UserRepository;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SitterProfileService {

    private final SitterProfileRepository repo;
    private final UserRepository userRepo;

    public SitterProfileService(SitterProfileRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    // ✅ CREATE + VALIDATION
    public SitterProfileResponse create(SitterProfileRequest req) {

        // 🔥 กันซ้ำ
        if (repo.existsByUserId(req.userId)) {
            throw new IllegalArgumentException("Sitter profile already exists");
        }

        User user = userRepo.findById(req.userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 🔥 role check
        if (user.getRole() != Role.SITTER) {
            throw new IllegalArgumentException("User is not a sitter");
        }

        // 🔥 validation เพิ่ม
        if (req.pricePerHour == null || req.pricePerHour < 0) {
            throw new IllegalArgumentException("Invalid price");
        }

        SitterProfile profile = new SitterProfile();
        profile.setBio(req.bio);
        profile.setPricePerHour(req.pricePerHour);
        profile.setExperience(req.experience);
        profile.setUser(user);

        repo.save(profile);

        return mapToResponse(profile);
    }

    // 🔥 PAGINATION + FILTER + SORT
    public Page<SitterProfileResponse> getAll(
            Double minPrice,
            Double maxPrice,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        // 🔥 กัน input พัง
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination");
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SitterProfile> result;

        if (minPrice != null && maxPrice != null) {

            if (minPrice > maxPrice) {
                throw new IllegalArgumentException("Invalid price range");
            }

            List<SitterProfile> list = repo.findByPricePerHourBetween(minPrice, maxPrice);
            result = new PageImpl<>(list, pageable, list.size());

        } else {
            result = repo.findAll(pageable);
        }

        return result.map(this::mapToResponse);
    }

    private SitterProfileResponse mapToResponse(SitterProfile profile) {
        SitterProfileResponse res = new SitterProfileResponse();
        res.id = profile.getId();
        res.bio = profile.getBio();
        res.pricePerHour = profile.getPricePerHour();
        res.experience = profile.getExperience();
        res.userId = profile.getUser().getId();
        return res;
    }
}