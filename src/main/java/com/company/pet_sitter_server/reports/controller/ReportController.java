package com.company.pet_sitter_server.reports.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.pet_sitter_server.common.security.JwtUtil;
import com.company.pet_sitter_server.reports.dto.ReportRequestDTO;
import com.company.pet_sitter_server.reports.dto.ReportResponseDTO;
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
            String token = header.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new RuntimeException("Unauthorized");
    }

    @PostMapping
    public ResponseEntity<ReportResponseDTO> submitReport(
            @Valid @RequestBody ReportRequestDTO request,
            HttpServletRequest httpRequest) {
        Long userId = extractUserIdFromToken(httpRequest);
        ReportResponseDTO response = reportService.submitReport(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
