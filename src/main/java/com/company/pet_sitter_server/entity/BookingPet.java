package com.company.pet_sitter_server.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "booking_pets")
@Data
public class BookingPet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private Long petId;
}
