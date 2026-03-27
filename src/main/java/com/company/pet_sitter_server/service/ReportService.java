package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.dto.report.ReportRequest;
import com.company.pet_sitter_server.dto.report.ReportResponse;
import com.company.pet_sitter_server.entity.Report;
import com.company.pet_sitter_server.enums.ReportStatus;
import com.company.pet_sitter_server.repository.ReportRepository;
import com.company.pet_sitter_server.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReportService {

    @Autowired private ReportRepository reportRepository;
    @Autowired private UserProfileRepository userProfileRepository;

    public ReportResponse createReport(Long reporterId, ReportRequest request) {
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setReportedSitterId(request.getReportedSitterId());
        report.setIssue(request.getIssue());
        report.setDescription(request.getDescription());
        report.setStatus(ReportStatus.NEW);
        return toResponse(reportRepository.save(report));
    }

    public List<ReportResponse> getMyReports(Long reporterId) {
        return reportRepository.findByReporterId(reporterId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ReportResponse toResponse(Report report) {
        ReportResponse r = new ReportResponse();
        r.setId(report.getId());
        r.setReporterId(report.getReporterId());
        r.setReportedSitterId(report.getReportedSitterId());
        r.setIssue(report.getIssue());
        r.setDescription(report.getDescription());
        r.setStatus(report.getStatus() != null ? report.getStatus().name() : null);
        r.setCreatedAt(report.getCreatedAt());
        userProfileRepository.findByUserId(report.getReporterId())
                .ifPresent(up -> r.setReporterName(up.getFullName()));
        userProfileRepository.findByUserId(report.getReportedSitterId())
                .ifPresent(up -> r.setReportedSitterName(up.getFullName()));
        return r;
    }
}
