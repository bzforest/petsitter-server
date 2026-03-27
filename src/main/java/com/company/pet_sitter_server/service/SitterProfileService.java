package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.dto.sitter.*;
import com.company.pet_sitter_server.entity.*;
import com.company.pet_sitter_server.enums.SitterStatus;
import com.company.pet_sitter_server.exception.AccessDeniedException;
import com.company.pet_sitter_server.exception.BadRequestException;
import com.company.pet_sitter_server.exception.NotFoundException;
import com.company.pet_sitter_server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class SitterProfileService {

    @Autowired private SitterProfileRepository sitterProfileRepository;
    @Autowired private SitterImageRepository sitterImageRepository;
    @Autowired private SitterOfferRepository sitterOfferRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private AddressRepository addressRepository;

    public Page<SitterProfileResponse> searchSitters(String petType, Double minRating,
                                                      Integer minExp, Pageable pageable) {
        return sitterProfileRepository.searchApproved(petType, minRating, minExp, pageable)
                .map(this::toResponse);
    }

    public SitterProfileResponse getSitterById(Long sitterProfileId) {
        SitterProfile profile = sitterProfileRepository.findById(sitterProfileId)
                .orElseThrow(() -> new NotFoundException("Sitter not found with id: " + sitterProfileId));
        if (profile.getStatus() != SitterStatus.APPROVED) {
            throw new NotFoundException("Sitter not found with id: " + sitterProfileId);
        }
        return toResponse(profile);
    }

    public SitterProfileResponse getMyProfile(Long userId) {
        SitterProfile profile = sitterProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    SitterProfile p = new SitterProfile();
                    p.setUserId(userId);
                    return sitterProfileRepository.save(p);
                });
        return toResponse(profile);
    }

    public SitterProfileResponse updateMyProfile(Long userId, SitterProfileRequest request) {
        SitterProfile profile = sitterProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    SitterProfile p = new SitterProfile();
                    p.setUserId(userId);
                    return p;
                });

        profile.setTradeName(request.getTradeName());
        profile.setBio(request.getBio());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setPetTypes(request.getPetTypes() != null
                ? String.join(",", request.getPetTypes()) : null);
        profile.setPhone(request.getPhone());
        profile.setIdNumber(request.getIdNumber());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setPlaceDescription(request.getPlaceDescription());

        if (request.getAddressLine() != null) {
            Address address = addressRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Address a = new Address();
                        a.setUserId(userId);
                        return a;
                    });
            address.setAddressLine(request.getAddressLine());
            address.setDistrict(request.getDistrict());
            address.setProvince(request.getProvince());
            address.setPostalCode(request.getPostalCode());
            Address savedAddress = addressRepository.save(address);
            profile.setAddressId(savedAddress.getId());
        }

        return toResponse(sitterProfileRepository.save(profile));
    }

    public SitterProfileResponse requestApproval(Long userId) {
        SitterProfile profile = sitterProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Sitter profile not found"));

        if (profile.getTradeName() == null || profile.getBio() == null) {
            throw new BadRequestException("Please complete your profile before requesting approval");
        }

        profile.setStatus(SitterStatus.WAITING_FOR_APPROVE);
        profile.setRejectReason(null);
        return toResponse(sitterProfileRepository.save(profile));
    }

    public SitterImage addImage(Long userId, String imageUrl) {
        SitterProfile profile = sitterProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Sitter profile not found"));
        long count = sitterImageRepository.findBySitterId(profile.getId()).size();
        if (count >= 5) {
            throw new BadRequestException("Maximum 5 images allowed");
        }
        SitterImage img = new SitterImage();
        img.setSitterId(profile.getId());
        img.setImageUrl(imageUrl);
        return sitterImageRepository.save(img);
    }

    public void deleteImage(Long userId, Long imageId) {
        SitterProfile profile = sitterProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Sitter profile not found"));
        SitterImage img = sitterImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));
        if (!img.getSitterId().equals(profile.getId())) {
            throw new AccessDeniedException("This image does not belong to you");
        }
        sitterImageRepository.deleteById(imageId);
    }

    public SitterOfferResponse addOffer(Long userId, SitterOfferRequest request) {
        sitterProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Sitter profile not found"));
        serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));
        SitterOffer offer = new SitterOffer();
        offer.setSitterId(userId);
        offer.setServiceId(request.getServiceId());
        offer.setPricePerHour(request.getPricePerHour());
        offer.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        return toOfferResponse(sitterOfferRepository.save(offer));
    }

    public SitterOfferResponse updateOffer(Long userId, Long offerId, SitterOfferRequest request) {
        SitterOffer offer = sitterOfferRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Service offer not found"));
        if (!offer.getSitterId().equals(userId)) {
            throw new AccessDeniedException("This offer does not belong to you");
        }
        offer.setPricePerHour(request.getPricePerHour());
        offer.setIsAvailable(request.getIsAvailable());
        return toOfferResponse(sitterOfferRepository.save(offer));
    }

    public void deleteOffer(Long userId, Long offerId) {
        SitterOffer offer = sitterOfferRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Service offer not found"));
        if (!offer.getSitterId().equals(userId)) {
            throw new AccessDeniedException("This offer does not belong to you");
        }
        sitterOfferRepository.deleteById(offerId);
    }

    public SitterProfileResponse toResponse(SitterProfile profile) {
        SitterProfileResponse r = new SitterProfileResponse();
        r.setId(profile.getId());
        r.setUserId(profile.getUserId());
        r.setStatus(profile.getStatus() != null ? profile.getStatus().name() : null);
        r.setRejectReason(profile.getRejectReason());
        r.setTradeName(profile.getTradeName());
        r.setBio(profile.getBio());
        r.setExperienceYears(profile.getExperienceYears());
        r.setPhone(profile.getPhone());
        r.setPlaceDescription(profile.getPlaceDescription());
        r.setRatingAvg(profile.getRatingAvg());

        if (profile.getPetTypes() != null && !profile.getPetTypes().isBlank()) {
            r.setPetTypes(Arrays.asList(profile.getPetTypes().split(",")));
        }

        userRepository.findById(profile.getUserId()).ifPresent(u -> r.setEmail(u.getEmail()));
        userProfileRepository.findByUserId(profile.getUserId())
                .ifPresent(up -> r.setProfileImage(up.getProfileImage()));

        if (profile.getAddressId() != null) {
            addressRepository.findById(profile.getAddressId()).ifPresent(a -> {
                r.setAddressLine(a.getAddressLine());
                r.setDistrict(a.getDistrict());
                r.setProvince(a.getProvince());
                r.setPostalCode(a.getPostalCode());
            });
        }

        r.setImageUrls(sitterImageRepository.findBySitterId(profile.getId())
                .stream().map(SitterImage::getImageUrl).toList());

        r.setServices(sitterOfferRepository.findBySitterId(profile.getUserId())
                .stream().map(this::toOfferResponse).toList());

        return r;
    }

    private SitterOfferResponse toOfferResponse(SitterOffer offer) {
        SitterOfferResponse r = new SitterOfferResponse();
        r.setId(offer.getId());
        r.setServiceId(offer.getServiceId());
        r.setPricePerHour(offer.getPricePerHour());
        r.setIsAvailable(offer.getIsAvailable());
        serviceRepository.findById(offer.getServiceId())
                .ifPresent(s -> r.setServiceName(s.getName()));
        return r;
    }
}
