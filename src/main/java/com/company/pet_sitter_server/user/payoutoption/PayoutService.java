package com.company.pet_sitter_server.user.payoutoption;

import com.company.pet_sitter_server.bookings.entity.Bookings;
import com.company.pet_sitter_server.bookings.repository.BookingRepository;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.repository.UserProfileRepository;
import com.company.pet_sitter_server.user.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PayoutService {

    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;
    private final UserProfileRepository userProfileRepo;

    public PayoutService(
            BookingRepository bookingRepo,
            UserRepository userRepo,
            UserProfileRepository userProfileRepo) {
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
        this.userProfileRepo = userProfileRepo;
    }

    /**
     * ดึง transactions ของ sitter ที่ login อยู่ (ค้นหาจาก email)
     * ดึงเฉพาะ booking ที่ sitterId ตรงกับ user นั้น
     */
    public List<PayoutResponse> getPayoutsForSitter(String email) {
        // หา user ของ sitter จาก email
        User sitter = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        // ดึง bookings ทั้งหมดของ sitter (ไม่มี pagination เพราะต้องรวม total)
        List<Bookings> bookings = bookingRepo.findAllBySitterId(sitter.getId());

        // แปลงเป็น PayoutResponse โดยกรองเฉพาะที่ status เป็น COMPLETED (Success)
        return bookings.stream()
                .filter(booking -> com.company.pet_sitter_server.enums.BookingStatus.COMPLETED.equals(booking.getStatus()))
                .map(booking -> mapToResponse(booking))
                .collect(Collectors.toList());
    }

    private PayoutResponse mapToResponse(Bookings booking) {
        PayoutResponse res = new PayoutResponse();

        // Transaction No. = booking id
        res.id = booking.getId();

        // Amount = total_price
        res.amount = booking.getTotalPrice();

        // From = user_id ของผู้จอง
        res.fromUserId = booking.getUserId();

        // Date = created_at (format: "25 Aug, 2023")
        if (booking.getCreatedAt() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH);
            res.date = booking.getCreatedAt().format(fmt);
        }

        // ชื่อผู้จอง — ดึงจาก UserProfile
        if (booking.getUserId() != null) {
            userProfileRepo.findByUserId(booking.getUserId()).ifPresentOrElse(
                    up -> res.fromName = up.getFullName() != null ? up.getFullName() : "Unknown",
                    () -> {
                        // fallback: ถ้าไม่มี UserProfile ให้ใช้ email จาก User
                        userRepo.findById(booking.getUserId()).ifPresent(u -> {
                            res.fromName = u.getEmail();
                        });
                        if (res.fromName == null) res.fromName = "Unknown";
                    }
            );
        } else {
            res.fromName = "Unknown";
        }

        return res;
    }
}
