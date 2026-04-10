package com.company.pet_sitter_server.chat.service;

import com.company.pet_sitter_server.chat.dto.MessageRequest;
import com.company.pet_sitter_server.chat.dto.MessageResponse;
import com.company.pet_sitter_server.chat.entity.Message;
import com.company.pet_sitter_server.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.company.pet_sitter_server.chat.dto.ChatInboxResponse;
import com.company.pet_sitter_server.user.repository.UserRepository;
import com.company.pet_sitter_server.user.repository.SitterProfileRepository;
import com.company.pet_sitter_server.user.entity.SitterProfile;
import com.company.pet_sitter_server.user.repository.UserProfileRepository;
import com.company.pet_sitter_server.user.entity.UserProfile; 
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private SitterProfileRepository sitterProfileRepository;

    //  ฟังก์ชันสำหรับบันทึกข้อความใหม่
    public MessageResponse sendMessage(MessageRequest request) {
        Message msg = new Message();
        msg.setSenderId(request.senderId);
        msg.setReceiverId(request.receiverId);
        msg.setContent(request.content);
        msg.setImageUrl(request.imageUrl);
        
        Message savedMsg = messageRepository.save(msg);
        MessageResponse response = convertToResponse(savedMsg);

        //  ให้ Server ตะโกนส่งข้อความนี้ไปที่ช่องทาง /topic/messages/{รหัสคนรับ}
        messagingTemplate.convertAndSend("/topic/messages/" + response.receiverId, response);

        return response; // ส่งกลับไปบอกคนส่งว่า "ส่งสำเร็จ" (ผ่าน HTTP ปกติ)
    }

    //  ฟังก์ชันสำหรับดึงประวัติแชทของคน 2 คน
    public List<MessageResponse> getChatHistory(Long user1, Long user2) {
        return messageRepository.findChatHistory(user1, user2)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ฟังก์ชันช่วยแปลง Entity เป็น DTO (Response)
    private MessageResponse convertToResponse(Message msg) {
        MessageResponse res = new MessageResponse();
        res.id = msg.getId();
        res.senderId = msg.getSenderId();
        res.receiverId = msg.getReceiverId();
        res.content = msg.getContent();
        res.imageUrl = msg.getImageUrl();
        res.isRead = msg.isRead();
        res.createdAt = msg.getCreatedAt();
        return res;
    }

    // 🟢 ฟังก์ชันใหม่: จัดกลุ่มข้อความเพื่อทำหน้า Inbox (รายชื่อฝั่งซ้าย)
    public List<ChatInboxResponse> getInbox(Long userId) {
        List<Message> allMessages = messageRepository.findAllUserMessages(userId);
        Map<Long, ChatInboxResponse> inboxMap = new LinkedHashMap<>();

        for (Message msg : allMessages) {
            Long partnerId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();

            if (!inboxMap.containsKey(partnerId)) {
                ChatInboxResponse inbox = new ChatInboxResponse(); 
                inbox.partnerId = partnerId;
                
                // 🟢 1. สร้างตัวแปรมารอรับค่า Default
                String finalName = "User ID: " + partnerId;
                String finalAvatar = null;

                // 🟢 2. ดึงข้อมูลมาทั้ง 2 ตารางเลย (ดักทางทั้งลูกค้าและพี่เลี้ยง)
                UserProfile uProfile = userProfileRepository.findByUserId(partnerId).orElse(null); 
                SitterProfile sProfile = sitterProfileRepository.findByUserId(partnerId).orElse(null);
                
                // 🟢 3. ถ้า Partner เป็น "ลูกค้า" (มีข้อมูลใน UserProfile)
                if (uProfile != null) {
                    if (uProfile.getFullName() != null && !uProfile.getFullName().isEmpty()) {
                        finalName = uProfile.getFullName();
                    }
                    if (uProfile.getProfileImage() != null && !uProfile.getProfileImage().isEmpty()) {
                        finalAvatar = uProfile.getProfileImage();
                    }
                }

                // 🟢 4. ถ้า Partner เป็น "พี่เลี้ยง" (ให้เอาข้อมูลร้านมาทับข้อมูลลูกค้าซะ)
                if (sProfile != null) {
                    if (sProfile.getTradeName() != null && !sProfile.getTradeName().isEmpty()) {
                        finalName = sProfile.getTradeName();
                    }
                    if (sProfile.getProfileImage() != null && !sProfile.getProfileImage().isEmpty()) {
                        finalAvatar = sProfile.getProfileImage();
                    }
                }

                // 🟢 5. จัดเก็บลง Inbox ตัวจริง
                inbox.partnerName = finalName;
                
                if (finalAvatar != null) {
                    inbox.partnerAvatar = finalAvatar;
                } else {
                    // ถ้าไม่มีรูปจริงๆ ให้เอาชื่อไปเจนเป็นรูปตัวอักษรย่อ (เช่น David Beckham -> DB)
                    inbox.partnerAvatar = "https://ui-avatars.com/api/?name=" + finalName.replace(" ", "+") + "&background=F3F4F6&color=374151";
                }
                
                inbox.lastMessage = msg.getImageUrl() != null ? "[Image]" : msg.getContent();
                inbox.lastMessageTime = msg.getCreatedAt();
                inbox.unreadCount = (!msg.isRead() && msg.getReceiverId().equals(userId)) ? 1 : 0;
                
                inboxMap.put(partnerId, inbox);
            } else {
                if (!msg.isRead() && msg.getReceiverId().equals(userId)) {
                    inboxMap.get(partnerId).unreadCount++;
                }
            }
        }
        
        return new ArrayList<>(inboxMap.values());
    }

    @Transactional
    public void markAsRead(Long userId, Long partnerId) {
        messageRepository.markMessagesAsRead(userId, partnerId);
    }
}