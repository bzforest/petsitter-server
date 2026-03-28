package com.company.pet_sitter_server.repository.sitter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.company.pet_sitter_server.entity.sitter.PetSitter;

import java.util.List;

@Repository
public interface PetSitterRepository extends JpaRepository<PetSitter, Long> {
    List<PetSitter> findByServiceTypeId(Long serviceId);
}
