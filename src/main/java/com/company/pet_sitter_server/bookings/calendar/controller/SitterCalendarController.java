package com.company.pet_sitter_server.bookings.calendar.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.pet_sitter_server.bookings.calendar.dto.SitterCalendarItemResponse;
import com.company.pet_sitter_server.bookings.calendar.service.SitterCalendarService;
import com.company.pet_sitter_server.common.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sitter-calendar")
@RequiredArgsConstructor
public class SitterCalendarController {
    private final SitterCalendarService sitterCalendarService;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<List<SitterCalendarItemResponse>> getMyCalendar(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.length() < 8) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);
        if (!"SITTER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long sitterId = jwtUtil.extractUserId(token);
        return ResponseEntity.ok(sitterCalendarService.getMyCalendar(sitterId, startDate, endDate));
    }
}
