package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.config.JwtHelper;
import com.company.pet_sitter_server.dto.message.MessageRequest;
import com.company.pet_sitter_server.dto.message.MessageResponse;
import com.company.pet_sitter_server.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired private MessageService messageService;
    @Autowired private JwtHelper jwtHelper;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody MessageRequest request) {
        return ResponseEntity.ok(
                messageService.sendMessage(jwtHelper.getCurrentUserId(), request));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<Long>> getConversations() {
        return ResponseEntity.ok(
                messageService.getConversationPartners(jwtHelper.getCurrentUserId()));
    }

    @GetMapping("/{otherId}")
    public ResponseEntity<List<MessageResponse>> getConversation(@PathVariable Long otherId) {
        return ResponseEntity.ok(
                messageService.getConversation(jwtHelper.getCurrentUserId(), otherId));
    }
}
