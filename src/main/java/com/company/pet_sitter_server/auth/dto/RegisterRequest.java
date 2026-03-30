package com.company.pet_sitter_server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 13, message = "Password must be at least 13 characters")
    private String password;

    // role ที่ user เลือกตอนสมัคร: USER หรือ SITTER (ADMIN จะไม่ให้สมัครเองได้)
    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Phone number is required")
    // ^0 → ต้องขึ้นต้นด้วย 0
    // [0-9]{9}$ → ตามด้วยตัวเลข 0-9 อีก 9 ตัว (รวมทั้งหมด 10 ตัว)
    @Pattern(regexp = "^0[0-9]{9}$", message = "Phone number must start with 0 and be 10 digits")
    private String phone;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getPhone() {
        return phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
