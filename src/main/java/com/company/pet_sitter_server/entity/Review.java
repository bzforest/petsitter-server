package com.company.pet_sitter_server.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private Long userId;
    private Long sitterId;
    private Integer rating;
    private String comment;
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
