package com.company.pet_sitter_server.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // ดึงค่า jwt.secret จาก application.properties มาใช้
    @Value("${jwt.secret}")
    private String secret;

    // ดึงค่า jwt.expiration (milliseconds) เช่น 86400000 = 24 ชั่วโมง
    @Value("${jwt.expiration}")
    private long expirationMs;

    // แปลง secret string เป็น SecretKey object ที่ JJWT ใช้ได้
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * สร้าง JWT token
     * ข้อมูลที่ฝังใน token (claims):
     *   - subject = email ของ user
     *   - "role"  = role ของ user (USER / SITTER / ADMIN)
     *   - "userId" = id ใน DB ของเรา
     */
    public String generateToken(String email, String role, Long userId) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * ดึง Claims ทั้งหมดออกจาก token
     * Claims = ข้อมูลที่ฝังอยู่ใน payload ของ JWT
     * ถ้า token หมดอายุหรือถูกแก้ไข จะโยน Exception
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ดึง email (subject) จาก token
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ดึง role จาก token
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ดึง userId จาก token
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    // ตรวจว่า token หมดอายุหรือยัง
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // ตรวจสอบ token ครบทุกอย่าง: ถูก sign ด้วย key ของเรา + ยังไม่หมดอายุ
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
