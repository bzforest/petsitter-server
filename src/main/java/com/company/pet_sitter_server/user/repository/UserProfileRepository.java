package com.company.pet_sitter_server.user.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.company.pet_sitter_server.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);
    Optional<UserProfile> findByUser_Id(Long userId);
    Optional<UserProfile> findByUser_Email(String email);
    List<UserProfile> findAllByUser_IdIn(Collection<Long> userIds);
}
