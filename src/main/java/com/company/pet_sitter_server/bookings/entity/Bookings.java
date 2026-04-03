package com.company.pet_sitter_server.bookings.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import com.company.pet_sitter_server.enums.BookingStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.List;
import java.util.ArrayList;
import lombok.Data;

@Entity
@Table(name = "bookings")
@Data
public class Bookings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // ID ของเจ้าของสัตว์เลี้ยง
    private Long sitterId; // ID ของพี่เลี้ยง
    
    @ElementCollection
    @CollectionTable(name = "booking_pets", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "pet_id")
    private List<Long> petIds = new ArrayList<>();



    // เก็บราคา ณ วันที่จองไว้ด้วย เผื่อเพื่อนเปลี่ยนราคาทีหลัง ประวัติเราจะได้ไม่เพี้ยน
    @Column(name = "price_per_hour")
    private Double pricePerHour;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    private Double totalPrice;

    private String paymentMethod; // "CREDIT_CARD" หรือ "CASH"
    private String stripePaymentIntentId; // ไว้เก็บ ID จาก Stripe เพื่อเช็คสถานะ

    private String noteToSitter;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
