package com.company.pet_sitter_server.reports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportRequestDTO {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Issue is required")
    @Size(max = 50, message = "Issue must not exceed 50 characters")
    private String issue;

    private String description;
}
