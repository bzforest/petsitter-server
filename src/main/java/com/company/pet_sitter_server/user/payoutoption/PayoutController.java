package com.company.pet_sitter_server.user.payoutoption;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payouts")
public class PayoutController {

    private final PayoutService payoutService;

    public PayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    /**
     * GET /api/payouts/me
     * ดึงรายการ Payout (transactions) ของ sitter ที่ login อยู่
     */
    @GetMapping("/me")
    public ResponseEntity<List<PayoutResponse>> getMyPayouts(Authentication auth) {
        List<PayoutResponse> payouts = payoutService.getPayoutsForSitter(auth.getName());
        return ResponseEntity.ok(payouts);
    }
}
