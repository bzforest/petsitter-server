package com.company.pet_sitter_server.user.service;

import com.company.pet_sitter_server.address.entity.Address;
import com.company.pet_sitter_server.address.repository.AddressRepository;
import com.company.pet_sitter_server.enums.Role;
import com.company.pet_sitter_server.enums.SitterStatus;
import com.company.pet_sitter_server.user.dto.SitterProfileRequest;
import com.company.pet_sitter_server.user.dto.SitterProfileResponse;
import com.company.pet_sitter_server.user.dto.SitterProfileUpdateRequest;
import com.company.pet_sitter_server.user.entity.SitterProfile;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.repository.SitterProfileRepository;
import com.company.pet_sitter_server.user.repository.UserRepository;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SitterProfileService {

    private static final int EXPERIENCE_MIN = 0;
    private static final int EXPERIENCE_MAX = 100;

    private final SitterProfileRepository repo;
    private final UserRepository userRepo;
    private final AddressRepository addressRepo;

    public SitterProfileService(SitterProfileRepository repo, UserRepository userRepo, AddressRepository addressRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.addressRepo = addressRepo;
    }

    // CREATE + VALIDATION
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
        validateExperience(req.experience);

        SitterProfile profile = new SitterProfile();
        profile.setUser(user);
        profile.setBio(req.bio);
        profile.setPricePerHour(req.pricePerHour);
        profile.setExperience(req.experience);
        profile.setTradeName(req.tradeName);
        profile.setPetTypes(req.petTypes);
        profile.setPlaceDescription(req.placeDescription);
        profile.setIdNumber(req.idNumber);
        profile.setDateOfBirth(req.dateOfBirth);
        profile.setPhone(user.getPhone());
        if (req.gallery != null) profile.setGallery(req.gallery);

        if (req.addressId != null) {
            Address address = addressRepo.findById(req.addressId)
                    .orElseThrow(() -> new IllegalArgumentException("Address not found"));
            profile.setAddress(address);
        }

        repo.save(profile);

        return mapToResponse(profile);
    }

    // GET ME — ดึง profile ของ user ที่ login อยู่ (ใช้ email จาก JWT)
    public SitterProfileResponse getMe(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SitterProfile profile = repo.findByUserId(user.getId())
                .orElseGet(() -> {
                    SitterProfile blank = new SitterProfile();
                    blank.setUser(user);
                    blank.setPhone(user.getPhone());
                    return repo.save(blank);
                });

        return mapToResponse(profile);
    }

    // UPDATE
    public SitterProfileResponse update(Long id, SitterProfileUpdateRequest req) {
        SitterProfile profile = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sitter profile not found"));

        if (req.bio != null) profile.setBio(req.bio);
        if (req.pricePerHour != null) profile.setPricePerHour(req.pricePerHour);
        if (req.experience != null) {
            validateExperience(req.experience);
            profile.setExperience(req.experience);
        }
        User user = profile.getUser();
        boolean userDirty = false;
        if (req.phone != null) {
            profile.setPhone(req.phone);
            if (user != null) {
                user.setPhone(req.phone);
                userDirty = true;
            }
        }
        if (req.email != null && user != null) {
            String newEmail = req.email.trim();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepo.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(newEmail);
            userDirty = true;
        }
        if (userDirty && user != null) {
            userRepo.save(user);
        }
        if (req.tradeName != null) profile.setTradeName(req.tradeName);
        if (req.petTypes != null) profile.setPetTypes(req.petTypes);
        if (req.services != null) profile.setServices(req.services);
        if (req.placeDescription != null) profile.setPlaceDescription(req.placeDescription);
        if (req.idNumber != null) profile.setIdNumber(req.idNumber);
        if (req.dateOfBirth != null) profile.setDateOfBirth(req.dateOfBirth);
        if (req.latitude != null) profile.setLatitude(req.latitude);
        if (req.longitude != null) profile.setLongitude(req.longitude);
        if (req.gallery != null) profile.setGallery(req.gallery);

        // อัปเดต Address — ถ้าส่ง addressLine มาให้ create/update Address record
        if (req.addressLine != null) {
            Address address = profile.getAddress() != null
                    ? profile.getAddress()
                    : new Address();
            address.setAddressLine(req.addressLine);
            address.setDistrict(req.district);
            address.setSubDistrict(req.subDistrict);
            address.setProvince(req.province);
            address.setPostalCode(req.postalCode);
            address.setUser(profile.getUser());
            addressRepo.save(address);
            profile.setAddress(address);
        }

        return mapToResponse(repo.save(profile));
    }

    // REQUEST APPROVAL
    public SitterProfileResponse requestApproval(Long id) {
        SitterProfile profile = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sitter profile not found"));
        profile.setStatus(SitterStatus.WAITING_FOR_APPROVE);
        profile.setRejectReason(null);
        return mapToResponse(repo.save(profile));
    }

    // PAGINATION + FILTER + SORT
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

        if (profile.getUser() != null) {
            res.userId = profile.getUser().getId();
            res.email = profile.getUser().getEmail();
        }

        res.bio = profile.getBio();
        res.pricePerHour = profile.getPricePerHour();
        res.experience = profile.getExperience();
        res.tradeName = profile.getTradeName();
        res.petTypes = profile.getPetTypes();
        res.services = profile.getServices();
        res.placeDescription = profile.getPlaceDescription();
        res.phone = profile.getPhone();
        res.idNumber = profile.getIdNumber();
        res.dateOfBirth = profile.getDateOfBirth();
        res.status = profile.getStatus();
        res.isApproved = profile.getIsApproved();
        res.ratingAvg = profile.getRatingAvg();
        res.rejectReason = profile.getRejectReason();
        res.latitude = profile.getLatitude();
        res.longitude = profile.getLongitude();
        res.gallery = profile.getGallery();

        if (profile.getAddress() != null) {
            Address addr = profile.getAddress();
            res.addressId = addr.getId();
            res.addressLine = addr.getAddressLine();
            res.district = addr.getDistrict();
            res.subDistrict = addr.getSubDistrict();
            res.province = addr.getProvince();
            res.postalCode = addr.getPostalCode();
        }

        return res;
    }

    /** จำนวนปีประสบการณ์ — null ได้, ถ้ามีค่าต้องอยู่ในช่วงที่สมเหตุสมผล */
    private static void validateExperience(Integer experience) {
        if (experience == null) return;
        if (experience < EXPERIENCE_MIN || experience > EXPERIENCE_MAX) {
            throw new IllegalArgumentException(
                    "Experience (years) must be between " + EXPERIENCE_MIN + " and " + EXPERIENCE_MAX);
        }
    }
}