package com.company.pet_sitter_server.entity;

import com.company.pet_sitter_server.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "text")
    private UserRole role = UserRole.USER;

    private Boolean isActive = true;

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
