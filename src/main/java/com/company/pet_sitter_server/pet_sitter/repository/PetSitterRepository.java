package com.company.pet_sitter_server.pet_sitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.company.pet_sitter_server.pet_sitter.entity.PetSitter;

import java.util.UUID;

@Repository
public interface PetSitterRepository extends JpaRepository<PetSitter, UUID> {
}