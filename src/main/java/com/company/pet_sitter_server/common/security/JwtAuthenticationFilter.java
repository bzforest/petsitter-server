package com.company.pet_sitter_server.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter ที่ทำงาน 1 ครั้งต่อ request (OncePerRequestFilter)
 * ทำหน้าที่:
 *   1. อ่าน Authorization header จาก request
 *   2. ตรวจสอบ JWT token
 *   3. ถ้า token ถูกต้อง → set Authentication ใน SecurityContext
 *      เพื่อบอก Spring Security ว่า "คนนี้ login แล้ว มี role นี้"
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // ดึง Authorization header — ค่าจะเป็น "Bearer eyJhbGci..."
        String authHeader = request.getHeader("Authorization");

        // ถ้าไม่มี header หรือไม่ได้ขึ้นต้นด้วย "Bearer " → ข้ามไปเลย (ไม่ set auth)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ตัด "Bearer " (7 ตัวอักษร) ออก เหลือแค่ token จริงๆ
        String token = authHeader.substring(7);

        // ตรวจสอบ token — ถ้า valid ให้ set authentication
        if (jwtUtil.isTokenValid(token)) {
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            // Spring Security ใช้ "ROLE_" prefix เพื่อ role-based auth
            // เช่น role = "USER" → authority = "ROLE_USER"
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

            // สร้าง Authentication object แล้ว set ลง SecurityContext
            // เมื่อ set แล้ว Spring Security จะรู้ว่า request นี้ authenticated แล้ว
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, List.of(authority));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // ส่งต่อให้ filter ถัดไป (หรือ controller)
        filterChain.doFilter(request, response);
    }
}
