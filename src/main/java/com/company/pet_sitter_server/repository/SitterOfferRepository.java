package com.company.pet_sitter_server.repository;

import com.company.pet_sitter_server.entity.SitterOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SitterOfferRepository extends JpaRepository<SitterOffer, Long> {
    List<SitterOffer> findBySitterId(Long sitterId);
}
