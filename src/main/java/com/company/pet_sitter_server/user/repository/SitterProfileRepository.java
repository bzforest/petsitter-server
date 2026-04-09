package com.company.pet_sitter_server.user.repository;

import com.company.pet_sitter_server.user.entity.SitterProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Collection;

public interface SitterProfileRepository
        extends JpaRepository<SitterProfile, Long>, JpaSpecificationExecutor<SitterProfile> {

    List<SitterProfile> findByPricePerHourBetween(Double min, Double max);

    boolean existsByUserId(Long userId);

    java.util.Optional<SitterProfile> findByUserId(Long userId);

    List<SitterProfile> findAllByUserIdIn(Collection<Long> userIds);
    
}