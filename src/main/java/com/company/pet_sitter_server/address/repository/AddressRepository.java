package com.company.pet_sitter_server.address.repository;

import com.company.pet_sitter_server.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    // 🔥 ใช้ดึง address ของ user
    List<Address> findByUserId(Long userId);
}