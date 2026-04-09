package com.company.pet_sitter_server.reports.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.company.pet_sitter_server.reports.entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByBookingIdAndReporterId(Long bookingId, Long reporterId);
}
