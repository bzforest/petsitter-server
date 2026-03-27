package com.company.pet_sitter_server.dto.report;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String reporterName;
    private Long reportedSitterId;
    private String reportedSitterName;
    private String issue;
    private String description;
    private String status;
    private OffsetDateTime createdAt;
}
