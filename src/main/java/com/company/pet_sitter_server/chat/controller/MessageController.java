package com.company.pet_sitter_server.chat.controller;

import com.company.pet_sitter_server.chat.dto.MessageRequest;
import com.company.pet_sitter_server.chat.dto.MessageResponse;
import com.company.pet_sitter_server.chat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.company.pet_sitter_server.chat.dto.ChatInboxResponse;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin
public class MessageController {

    @Autowired
    private MessageService messageService;

    //  API สำหรับส่งข้อความ (POST /api/messages)
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody MessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(request));
    }

    //  API สำหรับดึงประวัติแชท (GET /api/messages/history?user1=X&user2=Y)
    @GetMapping("/history")
    public ResponseEntity<List<MessageResponse>> getHistory(
            @RequestParam("user1") Long user1,
            @RequestParam("user2") Long user2) {
        return ResponseEntity.ok(messageService.getChatHistory(user1, user2));
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<ChatInboxResponse>> getInbox(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(messageService.getInbox(userId));
    }

    // 🟢 API สำหรับกดอัปเดตว่าอ่านข้อความแล้ว
    @PatchMapping("/read")
    public ResponseEntity<?> markAsRead(@RequestParam("userId") Long userId, @RequestParam("partnerId") Long partnerId) {
        messageService.markAsRead(userId, partnerId);
        return ResponseEntity.ok().build();
    }
}