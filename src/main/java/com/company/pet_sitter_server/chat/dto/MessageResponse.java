package com.company.pet_sitter_server.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class MessageResponse {
    
    public Long id;

    @JsonProperty("sender_id")
    public Long senderId;

    @JsonProperty("receiver_id")
    public Long receiverId;

    public String content;

    @JsonProperty("image_url")
    public String imageUrl;

    @JsonProperty("is_read")
    public boolean isRead;

    @JsonProperty("created_at")
    public LocalDateTime createdAt;
}