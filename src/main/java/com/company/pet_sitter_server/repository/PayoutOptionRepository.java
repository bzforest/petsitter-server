package com.company.pet_sitter_server.repository;

import com.company.pet_sitter_server.entity.PayoutOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PayoutOptionRepository extends JpaRepository<PayoutOption, Long> {
    Optional<PayoutOption> findBySitterId(Long sitterId);
}
