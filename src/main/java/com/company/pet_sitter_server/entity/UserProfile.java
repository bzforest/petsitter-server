package com.company.pet_sitter_server.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Data
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long userId;

    private String fullName;
    private String phone;
    private String profileImage;
    private String idNumber;
    private LocalDate dateOfBirth;
    private Double latitude;
    private Double longitude;
}
