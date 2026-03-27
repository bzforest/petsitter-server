package com.company.pet_sitter_server.repository;

import com.company.pet_sitter_server.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReporterId(Long reporterId);
    List<Report> findByReportedSitterId(Long reportedSitterId);
    Page<Report> findAll(Pageable pageable);
}
