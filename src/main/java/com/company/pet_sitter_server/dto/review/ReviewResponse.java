package com.company.pet_sitter_server.dto.review;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long bookingId;
    private Long userId;
    private String reviewerName;
    private String reviewerImage;
    private Long sitterId;
    private Integer rating;
    private String comment;
    private OffsetDateTime createdAt;
}
