package com.company.pet_sitter_server.user.repository;

import com.company.pet_sitter_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // ค้นหา user จาก supabase_id (ใช้ตอน login ด้วย Supabase token)
    Optional<User> findBySupabaseId(String supabaseId);
}