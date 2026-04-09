package com.company.pet_sitter_server.reports.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "reports")
@Data
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "reported_sitter_id", nullable = false)
    private Long reportedSitterId;

    @Column(name = "issue", nullable = false, length = 50)
    private String issue;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
