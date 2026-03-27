package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.dto.report.ReportResponse;
import com.company.pet_sitter_server.dto.review.ReviewResponse;
import com.company.pet_sitter_server.dto.sitter.SitterProfileResponse;
import com.company.pet_sitter_server.dto.user.UserProfileResponse;
import com.company.pet_sitter_server.entity.*;
import com.company.pet_sitter_server.enums.ReportStatus;
import com.company.pet_sitter_server.enums.SitterStatus;
import com.company.pet_sitter_server.exception.NotFoundException;
import com.company.pet_sitter_server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private SitterProfileRepository sitterProfileRepository;
    @Autowired private SitterProfileService sitterProfileService;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ReviewService reviewService;
    @Autowired private ReportRepository reportRepository;
    @Autowired private ReportService reportService;

    public Page<UserProfileResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(u -> {
            UserProfile profile = userProfileRepository.findByUserId(u.getId()).orElse(new UserProfile());
            return buildUserResponse(u, profile);
        });
    }

    public UserProfileResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(new UserProfile());
        return buildUserResponse(user, profile);
    }

    public void banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        user.setIsActive(false);
        userRepository.save(user);
    }

    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        user.setIsActive(true);
        userRepository.save(user);
    }

    public Page<SitterProfileResponse> getAllSitters(Pageable pageable) {
        return sitterProfileRepository.findAll(pageable)
                .map(sitterProfileService::toResponse);
    }

    public SitterProfileResponse getSitterById(Long sitterProfileId) {
        SitterProfile profile = sitterProfileRepository.findById(sitterProfileId)
                .orElseThrow(() -> new NotFoundException("Sitter not found with id: " + sitterProfileId));
        return sitterProfileService.toResponse(profile);
    }

    public SitterProfileResponse approveSitter(Long sitterProfileId) {
        SitterProfile profile = sitterProfileRepository.findById(sitterProfileId)
                .orElseThrow(() -> new NotFoundException("Sitter not found with id: " + sitterProfileId));
        profile.setStatus(SitterStatus.APPROVED);
        profile.setRejectReason(null);
        return sitterProfileService.toResponse(sitterProfileRepository.save(profile));
    }

    public SitterProfileResponse rejectSitter(Long sitterProfileId, String reason) {
        SitterProfile profile = sitterProfileRepository.findById(sitterProfileId)
                .orElseThrow(() -> new NotFoundException("Sitter not found with id: " + sitterProfileId));
        profile.setStatus(SitterStatus.REJECTED);
        profile.setRejectReason(reason);
        return sitterProfileService.toResponse(sitterProfileRepository.save(profile));
    }

    public Page<ReviewResponse> getSitterReviews(Long sitterProfileId, Pageable pageable) {
        SitterProfile profile = sitterProfileRepository.findById(sitterProfileId)
                .orElseThrow(() -> new NotFoundException("Sitter not found with id: " + sitterProfileId));
        return reviewRepository.findBySitterId(profile.getUserId(), pageable)
                .map(reviewService::toResponse);
    }

    public void deleteReview(Long reviewId) {
        reviewService.deleteReview(reviewId);
    }

    public Page<ReportResponse> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable).map(reportService::toResponse);
    }

    public ReportResponse getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found with id: " + reportId));
        if (report.getStatus() == ReportStatus.NEW) {
            report.setStatus(ReportStatus.PENDING);
            reportRepository.save(report);
        }
        return reportService.toResponse(report);
    }

    public ReportResponse resolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found with id: " + reportId));
        report.setStatus(ReportStatus.RESOLVED);
        return reportService.toResponse(reportRepository.save(report));
    }

    public ReportResponse cancelReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found with id: " + reportId));
        report.setStatus(ReportStatus.CANCELLED);
        return reportService.toResponse(reportRepository.save(report));
    }

    private UserProfileResponse buildUserResponse(User u, UserProfile profile) {
        UserProfileResponse r = new UserProfileResponse();
        r.setUserId(u.getId());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole().name());
        r.setFullName(profile.getFullName());
        r.setPhone(profile.getPhone());
        r.setProfileImage(profile.getProfileImage());
        r.setIdNumber(profile.getIdNumber());
        r.setDateOfBirth(profile.getDateOfBirth());
        return r;
    }
}
