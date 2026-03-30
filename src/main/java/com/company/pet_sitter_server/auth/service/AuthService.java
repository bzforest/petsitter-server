package com.company.pet_sitter_server.auth.service;

import com.company.pet_sitter_server.auth.client.SupabaseAuthClient;
import com.company.pet_sitter_server.auth.dto.AuthResponse;
import com.company.pet_sitter_server.auth.dto.LoginRequest;
import com.company.pet_sitter_server.auth.dto.RegisterRequest;
import com.company.pet_sitter_server.common.security.JwtUtil;
import com.company.pet_sitter_server.enums.Role;
import com.company.pet_sitter_server.enums.UserStatus;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

    private final SupabaseAuthClient supabaseAuthClient;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(SupabaseAuthClient supabaseAuthClient,
                       UserRepository userRepository,
                       JwtUtil jwtUtil) {
        this.supabaseAuthClient = supabaseAuthClient;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register flow:
     * 1. ตรวจ role ที่ส่งมา (ห้าม ADMIN สมัครเอง)
     * 2. เรียก Supabase Auth API เพื่อสร้าง user → Supabase จัดการ hash password
     * 3. บันทึก user ลง DB ของเรา (เก็บ supabaseId, email, role)
     * 4. ออก JWT ของเราที่มี role ฝังอยู่
     */
    @Transactional
    public AuthResponse register(RegisterRequest req) {

        // ห้ามสมัครเป็น ADMIN ผ่าน API นี้
        Role role;
        try {
            role = Role.valueOf(req.getRole().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid role. Must be USER or SITTER");
        }
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot register as ADMIN");
        }

        // ตรวจ email ซ้ำใน DB ของเราก่อน
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // เรียก Supabase Auth API → สร้าง user ใน Supabase
        // Supabase จะ hash password อย่างปลอดภัย (bcrypt)
        Map supabaseResponse = supabaseAuthClient.signUp(req.getEmail(), req.getPassword());

        // ดึง supabase user id จาก response
        // response จะมีรูปแบบ: { "id": "uuid...", "email": "...", ... }
        String supabaseUserId = (String) supabaseResponse.get("id");
        if (supabaseUserId == null) {
            throw new RuntimeException("Failed to create user in Supabase: " + supabaseResponse);
        }

        // บันทึก user ลง DB ของเรา (ไม่เก็บ password!)
        User user = new User();
        user.setSupabaseId(supabaseUserId);
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        // ออก JWT ของเราที่มี role และ userId ฝังอยู่
        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getId()
        );

        return new AuthResponse(token, savedUser.getEmail(), savedUser.getRole().name(), savedUser.getId());
    }

    /**
     * Login flow:
     * 1. เรียก Supabase Auth API ให้ตรวจ email/password → ถ้าผิดจะโยน Exception
     * 2. ดึง supabaseId จาก response
     * 3. หา user ใน DB ของเรา (จาก email)
     * 4. ตรวจว่า user ไม่ถูก ban
     * 5. ออก JWT ของเรา
     */
    public AuthResponse login(LoginRequest req) {

        // Supabase จะ throw error ถ้า email/password ผิด
        Map supabaseResponse = supabaseAuthClient.signIn(req.getEmail(), req.getPassword());

        // ดึง user object ออกจาก response
        // login response format: { "user": { "id": "uuid", "email": "..." }, "access_token": "..." }
        Map supabaseUser = (Map) supabaseResponse.get("user");
        if (supabaseUser == null) {
            throw new RuntimeException("Login failed");
        }

        // หา user ใน DB ของเราจาก email
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found in system"));

        // ตรวจสอบสถานะ — ถ้า BANNED ไม่ให้ login
        if (user.getStatus() == UserStatus.BANNED) {
            throw new IllegalArgumentException("Account is banned");
        }

        // ออก JWT ของเรา
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );

        return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getId());
    }
}
