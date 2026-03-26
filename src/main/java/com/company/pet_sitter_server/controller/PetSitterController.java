package com.company.pet_sitter_server.controller;

import com.company.pet_sitter_server.entity.PetSitter;
import com.company.pet_sitter_server.repository.PetSitterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sitters") // กำหนด URL หลัก
public class PetSitterController {

    @Autowired
    private PetSitterRepository repository;

    @GetMapping // ดึงข้อมูลทั้งหมด
    public List<PetSitter> getAllSitters() {
        return repository.findAll();
    }

    @PostMapping // ลองส่งข้อมูลใหม่เข้าไป
    public PetSitter createSitter(@RequestBody PetSitter sitter) {
        return repository.save(sitter);
    }
}