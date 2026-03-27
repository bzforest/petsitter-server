package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.dto.admin.AdminRejectRequest;
import com.company.pet_sitter_server.dto.report.ReportResponse;
import com.company.pet_sitter_server.dto.review.ReviewResponse;
import com.company.pet_sitter_server.dto.sitter.SitterProfileResponse;
import com.company.pet_sitter_server.dto.user.UserProfileResponse;
import com.company.pet_sitter_server.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")   // ← applies to ALL methods in this controller
public class AdminController {

    @Autowired private AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserProfileResponse>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<Void> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sitters")
    public ResponseEntity<Page<SitterProfileResponse>> getAllSitters(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllSitters(pageable));
    }

    @GetMapping("/sitters/{id}")
    public ResponseEntity<SitterProfileResponse> getSitterById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getSitterById(id));
    }

    @PutMapping("/sitters/{id}/approve")
    public ResponseEntity<SitterProfileResponse> approveSitter(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveSitter(id));
    }

    @PutMapping("/sitters/{id}/reject")
    public ResponseEntity<SitterProfileResponse> rejectSitter(
            @PathVariable Long id, @RequestBody AdminRejectRequest request) {
        return ResponseEntity.ok(adminService.rejectSitter(id, request.getReason()));
    }

    @GetMapping("/sitters/{id}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getSitterReviews(
            @PathVariable Long id, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(adminService.getSitterReviews(id, pageable));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        adminService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports")
    public ResponseEntity<Page<ReportResponse>> getAllReports(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllReports(pageable));
    }

    @GetMapping("/reports/{id}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getReportById(id));
    }

    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<ReportResponse> resolveReport(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.resolveReport(id));
    }

    @PutMapping("/reports/{id}/cancel")
    public ResponseEntity<ReportResponse> cancelReport(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.cancelReport(id));
    }
}
