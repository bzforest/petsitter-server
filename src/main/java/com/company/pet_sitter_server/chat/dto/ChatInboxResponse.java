package com.company.pet_sitter_server.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ChatInboxResponse {
    
    @JsonProperty("partner_id")
    public Long partnerId; // ID ของคนที่เราคุยด้วย

    @JsonProperty("partner_name")
    public String partnerName; // ชื่อของเขา

    @JsonProperty("partner_avatar")
    public String partnerAvatar; // รูปโปรไฟล์ของเขา

    @JsonProperty("last_message")
    public String lastMessage; // ข้อความล่าสุด

    @JsonProperty("unread_count")
    public int unreadCount; // จำนวนข้อความที่ยังไม่ได้อ่าน

    @JsonProperty("last_message_time")
    public LocalDateTime lastMessageTime; // เวลาของข้อความล่าสุด
}