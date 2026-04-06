package com.company.pet_sitter_server.common.config;

import com.company.pet_sitter_server.common.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ปิด CSRF — REST API ไม่ต้องใช้เพราะเราใช้ JWT แทน session
                .csrf(AbstractHttpConfigurer::disable)

                // เปิด CORS โดยใช้ config จาก WebConfig (WebMvcConfigurer)
                .cors(Customizer.withDefaults())

                // ใช้ Stateless session — Spring Security จะไม่สร้าง HTTP session
                // ทุก request ต้อง carry JWT เองทุกครั้ง
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // กำหนดกฎการเข้าถึง endpoint
                .authorizeHttpRequests(auth -> auth

                        // CORS preflight — ไม่มี Authorization; ต้อง permit ไม่งั้นได้ 403
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ Public endpoints — ไม่ต้อง login
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/services/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/sitter-profiles/**").permitAll()
                        .requestMatchers("/api/webhooks/**").permitAll()

                        // 🔐 ADMIN only
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/sitter-profiles/*/approve").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/sitter-profiles/*/reject").hasRole("ADMIN")

                        // 🔐 SITTER only
                        .requestMatchers(HttpMethod.POST, "/api/sitter-profiles/**").hasRole("SITTER")

                        // 🔐 ต้อง login (role ใดก็ได้)
                        .requestMatchers("/api/pets/**").authenticated()
                        .requestMatchers("/api/addresses/**").authenticated()
                        .requestMatchers("/api/user-profiles/**").authenticated()
                        .requestMatchers("/api/sitter-services/**").authenticated()

                        // endpoints อื่นๆ ที่ไม่ได้ระบุ → ต้อง login
                        .anyRequest().authenticated())

                // เพิ่ม JwtAuthenticationFilter ก่อน UsernamePasswordAuthenticationFilter
                // เพื่อให้ filter ของเราทำงานก่อน
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * กำหนด UserDetailsService bean เพื่อบอก Spring Security ว่าเราจัดการ auth
     * เองผ่าน JWT
     * ถ้าไม่กำหนด Spring Boot จะ auto-configure InMemoryUserDetailsManager
     * และแสดง warning "Using generated security password: ..."
     *
     * เราโยน UnsupportedOperationException เพราะระบบนี้ไม่ใช้ username/password
     * authentication
     * จาก Spring Security เลย — ใช้ JwtAuthenticationFilter แทนทั้งหมด
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UnsupportedOperationException("Authentication is handled via JWT");
        };
    }
}
