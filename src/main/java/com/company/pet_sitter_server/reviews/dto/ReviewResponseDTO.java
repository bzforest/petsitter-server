package com.company.pet_sitter_server.reviews.dto;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponseDTO {
    private Long id;
    private Long bookingId;
    private Long userId;
    private String userName;
    private String userProfileImage;
    private Integer rating;
    private String comment;
    private OffsetDateTime createdAt;
}
