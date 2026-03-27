package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.config.JwtHelper;
import com.company.pet_sitter_server.dto.payout.PayoutRequest;
import com.company.pet_sitter_server.dto.payout.PayoutResponse;
import com.company.pet_sitter_server.service.PayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sitters/me/payout")
public class PayoutController {

    @Autowired private PayoutService payoutService;
    @Autowired private JwtHelper jwtHelper;

    @GetMapping
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<PayoutResponse> getMyPayout() {
        return ResponseEntity.ok(payoutService.getMyPayout(jwtHelper.getCurrentUserId()));
    }

    @PutMapping
    @PreAuthorize("hasRole('SITTER')")
    public ResponseEntity<PayoutResponse> updateMyPayout(@RequestBody PayoutRequest request) {
        return ResponseEntity.ok(
                payoutService.updateMyPayout(jwtHelper.getCurrentUserId(), request));
    }
}
