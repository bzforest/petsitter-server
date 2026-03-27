package com.company.pet_sitter_server.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private Long senderId;
    private Long receiverId;
    private String message;
    private String imageUrl;
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
