package com.company.pet_sitter_server.reports.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.pet_sitter_server.bookings.entity.Bookings;
import com.company.pet_sitter_server.bookings.repository.BookingRepository;
import com.company.pet_sitter_server.enums.BookingStatus;
import com.company.pet_sitter_server.enums.Role;
import com.company.pet_sitter_server.reports.dto.ReportRequestDTO;
import com.company.pet_sitter_server.reports.dto.ReportResponseDTO;
import com.company.pet_sitter_server.reports.entity.Report;
import com.company.pet_sitter_server.reports.repository.ReportRepository;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.entity.UserProfile;
import com.company.pet_sitter_server.user.repository.UserProfileRepository;
import com.company.pet_sitter_server.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    // ─── User: Submit report ──────────────────────────────────────────────────

    @Transactional
    public ReportResponseDTO submitReport(ReportRequestDTO request, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() != Role.USER) {
            throw new SecurityException("Only users with role USER can submit a report");
        }

        Bookings booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(currentUserId)) {
            throw new SecurityException("You can only report your own bookings");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("You can only report a booking that has been completed");
        }

        if (reportRepository.existsByBookingIdAndReporterId(request.getBookingId(), currentUserId)) {
            throw new IllegalArgumentException("You have already submitted a report for this booking");
        }

        Report report = new Report();
        report.setBookingId(request.getBookingId());
        report.setReporterId(currentUserId);
        report.setReportedSitterId(booking.getSitterId());
        report.setIssue(request.getIssue());
        report.setDescription(request.getDescription());

        report = reportRepository.save(report);

        return mapToResponse(report);
    }

    // ─── Admin: Get all reports ───────────────────────────────────────────────

    public Page<ReportResponseDTO> getAllReports(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            return reportRepository.findByStatus(status, pageable).map(this::mapToResponse);
        }
        return reportRepository.findAll(pageable).map(this::mapToResponse);
    }

    // ─── Admin: Get single report + auto-change NEW_REPORT → PENDING ─────────

    @Transactional
    public ReportResponseDTO getReportByIdForAdmin(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if ("NEW_REPORT".equals(report.getStatus())) {
            report.setStatus("PENDING");
            report = reportRepository.save(report);
        }

        return mapToResponse(report);
    }

    // ─── Admin: Update status (RESOLVED / CANCELLED) ─────────────────────────

    @Transactional
    public ReportResponseDTO updateReportStatus(Long id, String newStatus) {
        if (!"RESOLVED".equals(newStatus) && !"CANCELLED".equals(newStatus)) {
            throw new IllegalArgumentException("Status must be RESOLVED or CANCELLED");
        }

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if ("RESOLVED".equals(report.getStatus()) || "CANCELLED".equals(report.getStatus())) {
            throw new IllegalStateException("This report has already been closed");
        }

        report.setStatus(newStatus);
        report = reportRepository.save(report);

        return mapToResponse(report);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ReportResponseDTO mapToResponse(Report report) {
        String reporterName = userProfileRepository.findByUserId(report.getReporterId())
                .map(UserProfile::getFullName)
                .orElse("Unknown User");

        String reportedSitterName = userProfileRepository.findByUserId(report.getReportedSitterId())
                .map(UserProfile::getFullName)
                .orElse("Unknown Sitter");

        return ReportResponseDTO.builder()
                .id(report.getId())
                .bookingId(report.getBookingId())
                .reporterId(report.getReporterId())
                .reporterName(reporterName)
                .reportedSitterId(report.getReportedSitterId())
                .reportedSitterName(reportedSitterName)
                .issue(report.getIssue())
                .description(report.getDescription())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
