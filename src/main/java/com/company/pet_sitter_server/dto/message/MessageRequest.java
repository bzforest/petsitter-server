package com.company.pet_sitter_server.dto.message;

import lombok.Data;

@Data
public class MessageRequest {
    private Long receiverId;
    private Long bookingId;
    private String message;
    private String imageUrl;
}
