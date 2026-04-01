package com.company.pet_sitter_server.user.repository;

import com.company.pet_sitter_server.user.entity.SitterProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SitterProfileRepository extends JpaRepository<SitterProfile, Long> {

    List<SitterProfile> findByPricePerHourBetween(Double min, Double max);

    boolean existsByUserId(Long userId);

    java.util.Optional<SitterProfile> findByUserId(Long userId);
}