package com.company.pet_sitter_server.bookings.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import lombok.Data;

@Data
public class BookingResponse {
    private Long id;
    private Long userId;
    private String ownerName;
    private Long sitterId;
    private String sitterProfileImage;
    private String sitterName;
    private String sitterFullName;
    private Long sitterProfileId;
    private List<String> petNames;
    private List<Long> petIds;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double totalHours;
    private Double pricePerHour;
    private String noteToSitter;
    private String clientSecret;
    private String paymentIntentId;
    private String paymentMethod;
    private Double totalPrice;
    private String status;
    private OffsetDateTime createdAt;
    private Long reviewId;
}
