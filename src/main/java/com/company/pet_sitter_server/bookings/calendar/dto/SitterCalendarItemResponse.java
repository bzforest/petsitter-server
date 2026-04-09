package com.company.pet_sitter_server.bookings.calendar.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class SitterCalendarItemResponse {
    private Long bookingId;
    private Long ownerId;
    private String ownerName;
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private String status;
}
