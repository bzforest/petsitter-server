package com.company.pet_sitter_server.repository;

import com.company.pet_sitter_server.entity.SitterImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SitterImageRepository extends JpaRepository<SitterImage, Long> {
    List<SitterImage> findBySitterId(Long sitterId);
}
