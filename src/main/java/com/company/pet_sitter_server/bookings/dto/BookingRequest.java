package com.company.pet_sitter_server.bookings.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;

@Data
public class BookingRequest {
    private Long userId;
    private Long sitterId;
    private List<Long> petIds;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String noteToSitter;
    private String paymentMethod;
}
