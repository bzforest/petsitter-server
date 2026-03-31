package com.company.pet_sitter_server.common.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    // ดึงค่าจาก application.properties หรือ application-local.properties
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        // คำสั่งนี้สำคัญมาก เป็นการบอก Stripe Library ว่า "ใช้คีย์นี้ในการคุยกับ Server นะ"
        Stripe.apiKey = stripeApiKey;
    }
}