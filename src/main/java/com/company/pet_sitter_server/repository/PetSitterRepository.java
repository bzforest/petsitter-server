package com.company.pet_sitter_server.repository;

import com.company.pet_sitter_server.entity.PetSitter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PetSitterRepository extends JpaRepository<PetSitter, UUID> {
}