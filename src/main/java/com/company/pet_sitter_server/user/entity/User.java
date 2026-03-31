package com.company.pet_sitter_server.user.entity;

import com.company.pet_sitter_server.enums.Role;
import com.company.pet_sitter_server.enums.UserStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UUID จาก Supabase Auth — ใช้ link user ของเรากับ Supabase
    @Column(name = "supabase_id", unique = true)
    private String supabaseId;

    @Column(nullable = false, unique = true)
    private String email;

    // nullable = true ที่ DB เพื่อรองรับ row เดิมที่ยังไม่มีค่า
    // การบังคับกรอกทำที่ DTO (RegisterRequest) ด้วย @NotBlank แทน
    @Column
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    private LocalDateTime createdAt;

    // Constructor
    public User() {}

    // Getters
    public Long getId() { return id; }
    public String getSupabaseId() { return supabaseId; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Role getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setSupabaseId(String supabaseId) { this.supabaseId = supabaseId; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(Role role) { this.role = role; }
    public void setStatus(UserStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
