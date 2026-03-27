package com.company.pet_sitter_server.user.repository;

import com.company.pet_sitter_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //  เช็ค email ซ้ำ
    Optional<User> findByEmail(String email);

    //  เพิ่มตัวนี้ (เร็วกว่า Optional)
    boolean existsByEmail(String email);
}