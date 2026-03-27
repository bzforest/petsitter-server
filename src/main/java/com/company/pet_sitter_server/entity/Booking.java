package com.company.pet_sitter_server.entity;

import com.company.pet_sitter_server.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long sitterId;
    private Long sitterServiceId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "text")
    private BookingStatus status = BookingStatus.WAITING_FOR_CONFIRM;

    private Double totalPrice;
    private String noteToSitter;
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
