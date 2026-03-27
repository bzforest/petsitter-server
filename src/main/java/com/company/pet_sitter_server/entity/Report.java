package com.company.pet_sitter_server.entity;

import com.company.pet_sitter_server.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reports")
@Data
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterId;
    private Long reportedSitterId;
    private String issue;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "text")
    private ReportStatus status = ReportStatus.NEW;

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
