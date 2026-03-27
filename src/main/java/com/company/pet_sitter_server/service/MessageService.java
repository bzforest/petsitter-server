package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.dto.message.MessageRequest;
import com.company.pet_sitter_server.dto.message.MessageResponse;
import com.company.pet_sitter_server.entity.Message;
import com.company.pet_sitter_server.repository.MessageRepository;
import com.company.pet_sitter_server.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageService {

    @Autowired private MessageRepository messageRepository;
    @Autowired private UserProfileRepository userProfileRepository;

    public MessageResponse sendMessage(Long senderId, MessageRequest request) {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(request.getReceiverId());
        msg.setBookingId(request.getBookingId());
        msg.setMessage(request.getMessage());
        msg.setImageUrl(request.getImageUrl());
        return toResponse(messageRepository.save(msg));
    }

    public List<MessageResponse> getConversation(Long userId, Long otherId) {
        return messageRepository.findConversation(userId, otherId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<Long> getConversationPartners(Long userId) {
        return messageRepository.findConversationPartnerIds(userId);
    }

    private MessageResponse toResponse(Message msg) {
        MessageResponse r = new MessageResponse();
        r.setId(msg.getId());
        r.setSenderId(msg.getSenderId());
        r.setReceiverId(msg.getReceiverId());
        r.setBookingId(msg.getBookingId());
        r.setMessage(msg.getMessage());
        r.setImageUrl(msg.getImageUrl());
        r.setCreatedAt(msg.getCreatedAt());
        userProfileRepository.findByUserId(msg.getSenderId()).ifPresent(up -> {
            r.setSenderName(up.getFullName());
            r.setSenderImage(up.getProfileImage());
        });
        return r;
    }
}
