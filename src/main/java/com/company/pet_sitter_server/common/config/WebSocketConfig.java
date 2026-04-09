package com.company.pet_sitter_server.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // กำหนดช่องทาง (Topic) สำหรับให้หน้าเว็บคอยฟัง (Subscribe)
        config.enableSimpleBroker("/topic");
        // กำหนดช่องทางสำหรับรับข้อความจากหน้าเว็บ
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // กำหนด URL สำหรับให้หน้าเว็บมาเชื่อมต่อ WebSocket (คล้ายๆ API)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // ยอมรับการเชื่อมต่อจากทุกหน้าเว็บ (CORS)
                .withSockJS(); // เปิดใช้ SockJS เผื่อเบราว์เซอร์ไม่รองรับ WebSocket ตรงๆ
    }
}