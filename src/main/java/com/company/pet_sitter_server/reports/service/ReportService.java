package com.company.pet_sitter_server.reports.service;

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
import com.company.pet_sitter_server.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

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

    private ReportResponseDTO mapToResponse(Report report) {
        return ReportResponseDTO.builder()
                .id(report.getId())
                .bookingId(report.getBookingId())
                .reporterId(report.getReporterId())
                .reportedSitterId(report.getReportedSitterId())
                .issue(report.getIssue())
                .description(report.getDescription())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
