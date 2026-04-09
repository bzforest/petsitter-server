package com.company.pet_sitter_server.auth.service;

import com.company.pet_sitter_server.auth.client.SupabaseAuthClient;
import com.company.pet_sitter_server.auth.dto.AuthResponse;
import com.company.pet_sitter_server.auth.dto.GoogleOAuthRequest;
import com.company.pet_sitter_server.auth.dto.LoginRequest;
import com.company.pet_sitter_server.auth.dto.RegisterRequest;
import com.company.pet_sitter_server.common.exception.RoleMismatchException;
import com.company.pet_sitter_server.common.security.JwtUtil;
import com.company.pet_sitter_server.enums.Role;
import com.company.pet_sitter_server.enums.UserStatus;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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
        Map<String, Object> supabaseResponse = supabaseAuthClient.signUp(req.getEmail(), req.getPassword());

        // ดึง supabase user id จาก response
        // response จะมีรูปแบบ: { "id": "uuid...", "email": "...", ... }
        Object userObject = supabaseResponse.get("user");
        if (!(userObject instanceof Map<?, ?> supabaseUser)) {
            throw new RuntimeException("Failed to create user in Supabase");
        }
        String supabaseUserId = (String) supabaseUser.get("id");

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
                savedUser.getId());

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
        Map<String, Object> supabaseResponse = supabaseAuthClient.signIn(req.getEmail(), req.getPassword());

        // ดึง user object ออกจาก response
        // login response format: { "user": { "id": "uuid", "email": "..." },
        // "access_token": "..." }
        Object userObject = supabaseResponse.get("user");
        if (!(userObject instanceof Map<?, ?>)) {
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
                user.getId());

        return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getId());
    }

    /**
     * Google OAuth login flow:
     * 1. รับ Supabase access_token จาก frontend (ได้มาหลัง Google OAuth callback)
     * 2. เรียก Supabase GET /auth/v1/user เพื่อ verify token และดึง user info
     * 3. ถ้า user ยังไม่มีใน DB → สร้างใหม่ (default role = USER)
     * 4. ถ้ามีแล้ว → ตรวจสถานะ (ห้าม BANNED)
     * 5. ออก JWT ของเรา
     *
     * ทำไมต้อง verify ที่ backend?
     * เพราะถ้า frontend ส่ง token มาโดยไม่ verify → ใครก็ได้อาจสร้าง fake token
     * การเรียก Supabase API ด้วย token นั้นเป็นการ verify ว่า token จริงและยังใช้งานได้
     */
    @Transactional
    public AuthResponse googleLogin(GoogleOAuthRequest req) {

        // verify token กับ Supabase — ถ้า token ไม่ถูกต้อง Supabase จะคืน 401
        // และ RestClient จะโยน exception ทำให้ request นี้ fail ทันที
        Map<String, Object> supabaseUser;
        try {
            supabaseUser = supabaseAuthClient.getUserByToken(req.getAccessToken());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or expired Google token");
        }

        // ดึง id และ email จาก Supabase user object
        String supabaseId = (String) supabaseUser.get("id");
        String email = (String) supabaseUser.get("email");

        if (supabaseId == null || email == null) {
            throw new RuntimeException("Failed to retrieve user info from Supabase");
        }

        // หา user ใน DB ของเรา — อาจมีจาก email/password register ก่อนหน้า
        Optional<User> existingUser = userRepository.findByEmail(email);

        // แปลง intendedRole string → Role enum (null ถ้า invalid หรือไม่ส่งมา)
        Role intendedRole = null;
        if (req.getIntendedRole() != null && !req.getIntendedRole().isBlank()) {
            try {
                Role parsed = Role.valueOf(req.getIntendedRole().toUpperCase());
                // ห้าม ADMIN — ป้องกัน user แอบ set role ตัวเองเป็น ADMIN
                if (parsed != Role.ADMIN) {
                    intendedRole = parsed;
                }
            } catch (IllegalArgumentException ignored) {
                // intendedRole string ไม่ถูกต้อง → ใช้ default USER
            }
        }

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();

            // ตรวจสถานะ — ถ้า BANNED ไม่ให้ login
            if (user.getStatus() == UserStatus.BANNED) {
                throw new IllegalArgumentException("Account is banned");
            }

            // ตรวจ role mismatch:
            // ถ้า user เลือก role ที่ไม่ตรงกับ role ใน DB → โยน RoleMismatchException (HTTP 409)
            // Frontend จะรับ error นี้และแสดง modal แจ้งเตือน พร้อมล้าง token ทั้งหมด
            if (intendedRole != null && intendedRole != user.getRole()) {
                throw new RoleMismatchException(user.getRole().name());
            }

            // อัปเดต supabaseId ถ้ายังไม่มี (เช่น เคย register ด้วย email/password มาก่อน)
            if (user.getSupabaseId() == null) {
                user.setSupabaseId(supabaseId);
            }

        } else {
            // User ใหม่ → สร้าง account อัตโนมัติ
            // ใช้ intendedRole ที่ user เลือกจาก modal, ถ้าไม่มีให้ default = USER
            Role newUserRole = (intendedRole != null) ? intendedRole : Role.USER;

            user = new User();
            user.setSupabaseId(supabaseId);
            user.setEmail(email);
            user.setRole(newUserRole);
            user.setStatus(UserStatus.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        }

        // ออก JWT ของเรา (เหมือนกับ login ปกติ)
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId());

        return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getId());
    }
}
