package com.company.pet_sitter_server.bookings.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class BookingResponse {
    private Long id;
    private String sitterName;
    private String petName;
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
}
