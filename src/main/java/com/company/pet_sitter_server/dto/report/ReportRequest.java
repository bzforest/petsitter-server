package com.company.pet_sitter_server.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {
    @NotNull(message = "Reported sitter ID is required")
    private Long reportedSitterId;

    @NotBlank(message = "Issue subject is required")
    private String issue;

    @NotBlank(message = "Description is required")
    private String description;
}
