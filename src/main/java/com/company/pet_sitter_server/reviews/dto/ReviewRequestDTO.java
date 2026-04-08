package com.company.pet_sitter_server.reviews.dto;

import lombok.Data;

@Data
public class ReviewRequestDTO {
    private Long bookingId;
    private Long sitterId;
    private Integer rating;
    private String comment;
}
