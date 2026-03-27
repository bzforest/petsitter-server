package com.company.pet_sitter_server.dto.message;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderImage;
    private Long receiverId;
    private Long bookingId;
    private String message;
    private String imageUrl;
    private OffsetDateTime createdAt;
}
