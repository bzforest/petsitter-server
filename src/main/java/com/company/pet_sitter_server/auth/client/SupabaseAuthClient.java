package com.company.pet_sitter_server.auth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * HTTP Client สำหรับเรียก Supabase Auth API
 *
 * Supabase Auth API ที่เราใช้:
 *   POST /auth/v1/signup        → สมัครสมาชิก
 *   POST /auth/v1/token         → login (รับ grant_type=password)
 *   DELETE /auth/v1/admin/users → ลบ user (admin only)
 */
@Component
public class SupabaseAuthClient {

    private final RestClient restClient;

    // ดึงค่า supabase.apiKey (service_role key) จาก properties
    // service_role key มีสิทธิ์เต็มที่ — ใช้สำหรับ admin operations
    @Value("${supabase.apiKey}")
    private String supabaseApiKey;

    public SupabaseAuthClient(@Value("${supabase.url}") String supabaseUrl) {
        // สร้าง RestClient ที่ point ไปที่ Supabase URL ของเรา
        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl)
                .build();
    }

    /**
     * สมัครสมาชิกใน Supabase Auth
     * POST /auth/v1/signup
     * Body: { "email": "...", "password": "..." }
     *
     * Response ที่ได้จะมี:
     *   - id: UUID ของ user ใน Supabase
     *   - email: email
     *   - access_token: JWT จาก Supabase (เราไม่ใช้ตัวนี้ เราจะออก JWT เองแทน)
     */
    public Map<String, Object> signUp(String email, String password) {
        return restClient.post()
                .uri("/auth/v1/signup")
                .header("apikey", supabaseApiKey)
                .header("Authorization", "Bearer " + supabaseApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", email, "password", password))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * Login ด้วย Supabase Auth
     * POST /auth/v1/token?grant_type=password
     * Body: { "email": "...", "password": "..." }
     *
     * Response จะมี:
     *   - user.id: UUID ของ user
     *   - user.email: email
     */
    public Map<String, Object> signIn(String email, String password) {
        return restClient.post()
                .uri("/auth/v1/token?grant_type=password")
                .header("apikey", supabaseApiKey)
                .header("Authorization", "Bearer " + supabaseApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", email, "password", password))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * ตรวจสอบ Supabase access_token ที่ได้จาก Google OAuth
     * GET /auth/v1/user
     * Authorization: Bearer <supabase_access_token>
     *
     * ถ้า token ถูกต้อง Supabase จะคืน user object:
     *   - id: UUID ของ user ใน Supabase
     *   - email: email ของ user (จาก Google account)
     *   - app_metadata.provider: "google"
     *
     * ถ้า token ไม่ถูกต้อง Supabase จะคืน 401 → RestClient จะโยน exception
     */
    public Map<String, Object> getUserByToken(String accessToken) {
        return restClient.get()
                .uri("/auth/v1/user")
                .header("apikey", supabaseApiKey)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
