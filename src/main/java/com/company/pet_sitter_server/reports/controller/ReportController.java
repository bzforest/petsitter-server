package com.company.pet_sitter_server.reports.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.pet_sitter_server.common.security.JwtUtil;
import com.company.pet_sitter_server.reports.dto.ReportRequestDTO;
import com.company.pet_sitter_server.reports.dto.ReportResponseDTO;
import com.company.pet_sitter_server.reports.dto.UpdateReportStatusRequest;
import com.company.pet_sitter_server.reports.service.ReportService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final JwtUtil jwtUtil;

    private Long extractUserIdFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return jwtUtil.extractUserId(header.substring(7));
        }
        throw new RuntimeException("Unauthorized");
    }

    // ─── User: Submit report ──────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ReportResponseDTO> submitReport(
            @Valid @RequestBody ReportRequestDTO request,
            HttpServletRequest httpRequest) {
        Long userId = extractUserIdFromToken(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.submitReport(request, userId));
    }

    // ─── Admin: Get all reports (with optional status filter + pagination) ────

    @GetMapping("/admin")
    public ResponseEntity<Page<ReportResponseDTO>> getAllReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(reportService.getAllReports(status, pageable));
    }

    // ─── Admin: Get single report detail (auto NEW_REPORT → PENDING) ─────────

    @GetMapping("/admin/{id}")
    public ResponseEntity<ReportResponseDTO> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportByIdForAdmin(id));
    }

    // ─── Admin: Update status (RESOLVED / CANCELLED) ─────────────────────────

    @PatchMapping("/admin/{id}/status")
    public ResponseEntity<ReportResponseDTO> updateReportStatus(
            @PathVariable Long id,
            @RequestBody UpdateReportStatusRequest request) {
        return ResponseEntity.ok(reportService.updateReportStatus(id, request.getStatus()));
    }
}
