package com.company.pet_sitter_server.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageRequest {
    
    @JsonProperty("sender_id")
    public Long senderId; // ใครเป็นคนส่ง

    @JsonProperty("receiver_id")
    public Long receiverId; // ส่งหาใคร

    public String content; // ข้อความว่าอะไร

    @JsonProperty("image_url")
    public String imageUrl; // มีแนบรูปมาด้วยไหม
}