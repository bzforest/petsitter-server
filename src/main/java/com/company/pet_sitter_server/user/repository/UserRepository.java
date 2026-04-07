package com.company.pet_sitter_server.user.repository;

import com.company.pet_sitter_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.Collection;

public interface UserRepository extends JpaRepository<User, Long> {

    // ค้นหา user จาก email
    Optional<User> findByEmail(String email);

    // ตรวจสอบว่า email นี้มีอยู่ในระบบหรือยัง
    boolean existsByEmail(String email);

    // ค้นหา user จาก supabase_id (ใช้ตอน login ด้วย Supabase token)
    Optional<User> findBySupabaseId(String supabaseId);

    List<User> findAllByIdIn(Collection<Long> ids);
}