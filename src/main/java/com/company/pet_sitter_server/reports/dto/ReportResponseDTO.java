package com.company.pet_sitter_server.reports.dto;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportResponseDTO {

    private Long id;
    private Long bookingId;
    private Long reporterId;
    private String reporterName;
    private Long reportedSitterId;
    private String reportedSitterName;
    private String issue;
    private String description;
    private String status;
    private OffsetDateTime createdAt;
}
