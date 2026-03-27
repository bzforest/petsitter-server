package com.company.pet_sitter_server.entity;

import com.company.pet_sitter_server.enums.SitterStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "sitter_profiles")
@Data
public class SitterProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long userId;

    private String tradeName;
    private String bio;
    private Integer experienceYears;
    private String petTypes;
    private String phone;
    private String idNumber;
    private LocalDate dateOfBirth;
    private String placeDescription;
    private Long addressId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "text")
    private SitterStatus status = SitterStatus.WAITING_FOR_APPROVE;

    private String rejectReason;

    @Column(columnDefinition = "numeric default 0")
    private Double ratingAvg = 0.0;
}
