package com.company.pet_sitter_server.user.repository;

import com.company.pet_sitter_server.user.entity.SitterProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SitterProfileRepository extends JpaRepository<SitterProfile, Long> {

    //  filter ราคา
    List<SitterProfile> findByPricePerHourBetween(Double min, Double max);

    //  กัน 1 user มีได้ 1 profile
    boolean existsByUserId(Long userId);
}