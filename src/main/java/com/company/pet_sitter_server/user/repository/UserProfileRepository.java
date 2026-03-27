package com.company.pet_sitter_server.user.repository;

import com.company.pet_sitter_server.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
