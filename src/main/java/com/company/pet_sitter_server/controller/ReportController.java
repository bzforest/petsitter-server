package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.config.JwtHelper;
import com.company.pet_sitter_server.dto.report.ReportRequest;
import com.company.pet_sitter_server.dto.report.ReportResponse;
import com.company.pet_sitter_server.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired private ReportService reportService;
    @Autowired private JwtHelper jwtHelper;

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(
                reportService.createReport(jwtHelper.getCurrentUserId(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReportResponse>> getMyReports() {
        return ResponseEntity.ok(reportService.getMyReports(jwtHelper.getCurrentUserId()));
    }
}
