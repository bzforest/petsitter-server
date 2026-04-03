package com.company.pet_sitter_server.user.service;

import com.company.pet_sitter_server.address.entity.Address;
import com.company.pet_sitter_server.address.repository.AddressRepository;
import com.company.pet_sitter_server.address.util.ThaiAddressNormalizer;
import com.company.pet_sitter_server.enums.Role;
import com.company.pet_sitter_server.enums.SitterStatus;
import com.company.pet_sitter_server.user.dto.SitterProfileRequest;
import com.company.pet_sitter_server.user.dto.SitterProfileResponse;
import com.company.pet_sitter_server.user.dto.SitterProfileUpdateRequest;
import com.company.pet_sitter_server.user.entity.SitterProfile;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.entity.UserProfile;
import com.company.pet_sitter_server.user.repository.SitterProfileRepository;
import com.company.pet_sitter_server.user.repository.UserProfileRepository;
import com.company.pet_sitter_server.user.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SitterProfileService {

    private static final int EXPERIENCE_MIN = 0;
    private static final int EXPERIENCE_MAX = 100;

    private final SitterProfileRepository repo;
    private final UserRepository userRepo;
    private final AddressRepository addressRepo;
    private final UserProfileRepository userProfileRepo;

    public SitterProfileService(
            SitterProfileRepository repo,
            UserRepository userRepo,
            AddressRepository addressRepo,
            UserProfileRepository userProfileRepo
    ) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.addressRepo = addressRepo;
        this.userProfileRepo = userProfileRepo;
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
        profile.setServicesDescription(req.servicesDescription);
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

    // UPDATE — ข้อมูลเต็ม (trade / gallery / map / address) รับได้เฉพาะเมื่อ APPROVED เท่านั้น
    public SitterProfileResponse update(Long id, SitterProfileUpdateRequest req) {
        SitterProfile profile = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sitter profile not found"));

        User user = profile.getUser();
        boolean approved = profile.getStatus() == SitterStatus.APPROVED;

        if (req.fullName != null && user != null) {
            upsertUserFullName(user, req.fullName);
        }
        if (req.profileImage != null) {
            String p = req.profileImage.trim();
            profile.setProfileImage(p.isEmpty() ? null : p);
        }
        if (req.bio != null) profile.setBio(req.bio);

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

        if (!approved) {
            return mapToResponse(repo.save(profile));
        }

        if (req.pricePerHour != null) profile.setPricePerHour(req.pricePerHour);
        if (req.experience != null) {
            validateExperience(req.experience);
            profile.setExperience(req.experience);
        }
        if (req.tradeName != null) profile.setTradeName(req.tradeName);
        if (req.petTypes != null) profile.setPetTypes(req.petTypes);
        if (req.services != null) profile.setServices(req.services);
        if (req.placeDescription != null) profile.setPlaceDescription(req.placeDescription);
        if (req.idNumber != null) profile.setIdNumber(req.idNumber);
        if (req.dateOfBirth != null) profile.setDateOfBirth(req.dateOfBirth);
        if (req.latitude != null) profile.setLatitude(req.latitude);
        if (req.longitude != null) profile.setLongitude(req.longitude);
        if (req.gallery != null) {
            if (req.gallery.size() > 10) {
                throw new IllegalArgumentException("Gallery must have at most 10 images");
            }
            profile.setGallery(req.gallery);
        }

        if (req.addressLine != null) {
            Address address = profile.getAddress() != null
                    ? profile.getAddress()
                    : new Address();
            address.setAddressLine(req.addressLine);
            ThaiAddressNormalizer.FlatThaiAddress triple =
                    ThaiAddressNormalizer.reconcileBangkokFlatFields(req.province, req.district, req.subDistrict);
            address.setDistrict(ThaiAddressNormalizer.normalizeDistrict(triple.district));
            address.setSubDistrict(ThaiAddressNormalizer.normalizeSubDistrict(triple.subDistrict));
            address.setProvince(ThaiAddressNormalizer.normalizeProvince(triple.province));
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
        User user = profile.getUser();
        if (user == null) {
            throw new IllegalArgumentException("Invalid profile");
        }

        String fullName = userProfileRepo.findByUser_Id(user.getId())
                .map(UserProfile::getFullName)
                .map(String::trim)
                .orElse("");
        if (fullName.isEmpty()) {
            throw new IllegalArgumentException("Full name is required before requesting approval");
        }

        String phone = profile.getPhone();
        if (phone == null || !phone.matches("^\\d{10}$")) {
            throw new IllegalArgumentException("Phone number must be exactly 10 digits");
        }

        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        int at = email.indexOf('@');
        if (at <= 0 || !email.substring(at + 1).contains(".")) {
            throw new IllegalArgumentException("Enter a valid email address");
        }

        String bio = profile.getBio();
        if (bio == null || bio.trim().isEmpty()) {
            throw new IllegalArgumentException("Introduction is required before requesting approval");
        }

        profile.setStatus(SitterStatus.WAITING_FOR_APPROVE);
        profile.setRejectReason(null);
        return mapToResponse(repo.save(profile));
    }

    // PAGINATION + FILTER + SORT
    public Page<SitterProfileResponse> getAll(
            Double minPrice,
            Double maxPrice,
            String query,
            List<String> petTypes,
            Integer rating,
            String experience,
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

        Specification<SitterProfile> spec = (root, queryObj, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            // 🚫 ซ่อนข้อมูลที่ไม่มีชื่อบริการ (TradeName)
            predicates.add(cb.isNotNull(root.get("tradeName")));
            predicates.add(cb.notEqual(root.get("tradeName"), ""));

            // เฉพาะที่ APPROVED เท่านั้น (ถ้าต้องการ Filter อัตโนมัติในอนาคต)
            // predicates.add(cb.equal(root.get("isApproved"), true));

            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerHour"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("pricePerHour"), maxPrice));

            // ค้นหาตามชื่อ (TradeName)
            if (query != null && !query.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("tradeName")), "%" + query.toLowerCase() + "%"));
            }

            // ค้นหาตามชนิดสัตว์เลี้ยง (petTypes ใน DB เป็น string เช่น "Dog, Cat")
            if (petTypes != null && !petTypes.isEmpty()) {
                var petPredicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
                for (String type : petTypes) {
                    petPredicates.add(cb.like(cb.lower(root.get("petTypes")), "%" + type.toLowerCase() + "%"));
                }
                predicates.add(cb.or(petPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            // ค้นหาตาม Rating (ratingAvg >= ค่าที่ส่งมา)
            if (rating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ratingAvg"), rating.doubleValue()));
            }

            // ค้นหาตาม Experience (ปี) — จะกรองก็ต่อเมื่อไม่ใช่ "All Experience" (ค่าว่าง)
            if (experience != null && !experience.isBlank()) {
                if (experience.equals("0-2 Years")) {
                    predicates.add(cb.between(root.get("experience"), 0, 2));
                } else if (experience.equals("3-5 Years")) {
                    predicates.add(cb.between(root.get("experience"), 3, 5));
                } else if (experience.equals("5+ Years")) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("experience"), 5));
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<SitterProfile> result = repo.findAll(spec, pageable);

        return result.map(this::mapToResponse);
    }

    public SitterProfileResponse getById(Long id) {
        SitterProfile profile = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sitter Profile not found with id: " + id));
        return mapToResponse(profile);
    }

    private SitterProfileResponse mapToResponse(SitterProfile profile) {
        SitterProfileResponse res = new SitterProfileResponse();
        res.id = profile.getId();

        if (profile.getUser() != null) {
            res.userId = profile.getUser().getId();
            res.email = profile.getUser().getEmail();
            res.fullName = userProfileRepo.findByUser_Id(profile.getUser().getId())
                    .map(UserProfile::getFullName)
                    .orElse(null);
        }

        res.profileImage = profile.getProfileImage();
        res.bio = profile.getBio();
        res.pricePerHour = profile.getPricePerHour();
        res.experience = profile.getExperience();
        res.servicesDescription = profile.getServicesDescription();
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

        // Fetch User Profile for fullName and profileImage
        if (profile.getUser() != null) {
            userProfileRepo.findByUserId(profile.getUser().getId()).ifPresent(up -> {
                res.fullName = up.getFullName();
                res.profileImage = up.getProfileImage();
            });
        }

        if (profile.getAddress() != null) {
            Address addr = profile.getAddress();
            res.addressId = addr.getId();
            res.addressLine = addr.getAddressLine();
            res.district = ThaiAddressNormalizer.normalizeDistrict(addr.getDistrict());
            res.subDistrict = ThaiAddressNormalizer.normalizeSubDistrict(addr.getSubDistrict());
            res.province = ThaiAddressNormalizer.normalizeProvince(addr.getProvince());
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

    private void upsertUserFullName(User user, String fullName) {
        String trimmed = fullName == null ? "" : fullName.trim();
        UserProfile up = userProfileRepo.findByUser_Id(user.getId()).orElseGet(() -> {
            UserProfile p = new UserProfile();
            p.setUser(user);
            p.setPhone(user.getPhone());
            return p;
        });
        up.setFullName(trimmed.isEmpty() ? null : trimmed);
        userProfileRepo.save(up);
    }
}