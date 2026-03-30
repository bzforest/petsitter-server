package com.company.pet_sitter_server.sitter_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.company.pet_sitter_server.sitter_service.entity.SitterService;

import java.util.List;

@Repository
public interface SitterServiceRepository extends JpaRepository<SitterService, Long> {
    List<SitterService> findByServiceTypeId(Long serviceId);
}
