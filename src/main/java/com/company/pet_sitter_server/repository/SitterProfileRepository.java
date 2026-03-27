package com.company.pet_sitter_server.repository;

import com.company.pet_sitter_server.entity.SitterProfile;
import com.company.pet_sitter_server.enums.SitterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SitterProfileRepository extends JpaRepository<SitterProfile, Long> {
    Optional<SitterProfile> findByUserId(Long userId);
    Page<SitterProfile> findAll(Pageable pageable);
    Page<SitterProfile> findByStatus(SitterStatus status, Pageable pageable);

    @Query("SELECT s FROM SitterProfile s WHERE s.status = 'APPROVED' " +
           "AND (:petType IS NULL OR s.petTypes LIKE %:petType%) " +
           "AND (:minRating IS NULL OR s.ratingAvg >= :minRating) " +
           "AND (:minExp IS NULL OR s.experienceYears >= :minExp)")
    Page<SitterProfile> searchApproved(@Param("petType") String petType,
                                       @Param("minRating") Double minRating,
                                       @Param("minExp") Integer minExp,
                                       Pageable pageable);
}
