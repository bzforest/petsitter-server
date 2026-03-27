package com.company.pet_sitter_server.dto.booking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class BookingRequest {
    @NotNull(message = "Sitter ID is required")
    private Long sitterId;

    @NotNull(message = "Service ID is required")
    private Long sitterServiceId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotEmpty(message = "At least one pet must be selected")
    private List<Long> petIds;

    private String noteToSitter;
}
