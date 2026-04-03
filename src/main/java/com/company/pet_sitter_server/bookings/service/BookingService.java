package com.company.pet_sitter_server.bookings.service;

import com.company.pet_sitter_server.bookings.dto.BookingRequest;
import com.company.pet_sitter_server.bookings.dto.BookingResponse;
import com.company.pet_sitter_server.bookings.dto.StripePaymentResponse;
import com.company.pet_sitter_server.bookings.entity.Bookings;
import com.company.pet_sitter_server.bookings.repository.BookingRepository;
import com.company.pet_sitter_server.enums.BookingStatus;
import com.company.pet_sitter_server.pets.repository.PetRepository;
import com.company.pet_sitter_server.user.entity.SitterProfile;
import com.company.pet_sitter_server.user.repository.SitterProfileRepository;
import com.company.pet_sitter_server.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // ใช้ constructor แทน autowired
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SitterProfileRepository sitterProfileRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final StripeService stripeService;
    private final ZoneId BANGKOK_ZONE = ZoneId.of("Asia/Bangkok");

    // ============================================================
    // POST /api/bookings — สร้าง Booking ใหม่
    // ============================================================
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // ดึงข้อมูล SitterProfile เพื่อดึง pricePerHour
        SitterProfile sitterProfile = sitterProfileRepository.findById(request.getSitterId())
                .orElseThrow(() -> new RuntimeException("Sitter Profile not found"));

        // คำนวณเวลาและราคา
        ZonedDateTime startBangkok = ZonedDateTime.of(
                request.getStartDate(),
                request.getStartTime(),
                BANGKOK_ZONE);

        ZonedDateTime endBangkok = ZonedDateTime.of(
                request.getEndDate(),
                request.getEndTime(),
                BANGKOK_ZONE);

        if (endBangkok.isBefore(startBangkok)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        long minutes = Duration.between(startBangkok, endBangkok).toMinutes();
        double hours = minutes / 60.0;
        
        // 200 THB/hr for 1st pet, 100 THB/hr for extra pets
        Double pricePerHour = sitterProfile.getPricePerHour();
        if (pricePerHour == null || pricePerHour <= 0) {
            pricePerHour = 200.0;
        }

        int numPets = request.getPetIds().size();
        double totalPrice = (pricePerHour * hours) + (100 * hours * (numPets - 1));

        // แมพข้อมูลลง Entity
        Bookings booking = new Bookings();
        booking.setUserId(request.getUserId());
        booking.setSitterId(sitterProfile.getUser().getId());
        booking.setPetIds(request.getPetIds());

        booking.setPricePerHour(pricePerHour);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setTotalPrice(totalPrice);
        booking.setNoteToSitter(request.getNoteToSitter());
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentMethod(request.getPaymentMethod());

        // สำหรับ CREDIT_CARD: สร้าง PaymentIntent ที่ Stripe
        String clientSecret = null;
        if ("CREDIT_CARD".equals(request.getPaymentMethod())) {
            try {
                StripePaymentResponse stripeRes = stripeService.createPaymentIntent(totalPrice);
                booking.setStripePaymentIntentId(stripeRes.getPaymentIntentId()); // เก็บ "pi_xxx" ใน DB
                clientSecret = stripeRes.getClientSecret(); // ส่งให้ Frontend
            } catch (Exception e) {
                throw new RuntimeException("Stripe error: " + e.getMessage());
            }
        }

        Bookings savedBooking = bookingRepository.save(booking);
        return toResponse(savedBooking, clientSecret);
    }

    // ============================================================
    // GET /api/bookings/{id} — ดึง Booking detail (Success Page)
    // ============================================================
    public BookingResponse getBookingById(Long id) {
        Bookings booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));
        return toResponse(booking, null);
    }

    // ============================================================
    // GET /api/bookings/user/{userId} — ดึง Booking history ของ user
    // ============================================================
    public List<BookingResponse> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(b -> toResponse(b, null))
                .collect(Collectors.toList());
    }
    
    // ============================================================
    // GET /api/bookings/sitter/me — ดึงรายการจองที่ Sitter คนนี้ถูกจอง
    // ============================================================
    public List<BookingResponse> getBookingsBySitter(Long sitterId) {
        return bookingRepository.findBySitterIdOrderByCreatedAtDesc(sitterId)
                .stream()
                .map(b -> toResponse(b, null))
                .collect(Collectors.toList());
    }

    // ============================================================
    // PATCH /api/bookings/{id}/confirm-cash — ยืนยัน Cash payment
    // ============================================================
    @Transactional
    public BookingResponse confirmCash(Long id) {
        Bookings booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));

        if (!BookingStatus.PENDING.equals(booking.getStatus())) {
            throw new IllegalArgumentException("Booking is not in PENDING status");
        }
        if (!"CASH".equals(booking.getPaymentMethod())) {
            throw new IllegalArgumentException("This booking is not a cash payment");
        }

        booking.setStatus(BookingStatus.PAID);
        return toResponse(bookingRepository.save(booking), null);
    }

    // ============================================================
    // PATCH /api/bookings/{id}/cancel — ยกเลิก Booking
    // ============================================================
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        Bookings booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));

        if (BookingStatus.COMPLETED.equals(booking.getStatus())) {
            throw new IllegalArgumentException("Cannot cancel a completed booking");
        }
        if (BookingStatus.CANCELLED.equals(booking.getStatus())) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking), null);
    }

    // ============================================================
    // PATCH /api/bookings/{id}/confirm — Sitter ยอมรับงาน (CONFIRMED)
    // ============================================================
    @Transactional
    public BookingResponse confirmBooking(Long id, Long sitterId) {
        Bookings booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));

        if (!booking.getSitterId().equals(sitterId)) {
            throw new SecurityException("You are not authorized to confirm this booking");
        }

        if (BookingStatus.CANCELLED.equals(booking.getStatus()) || BookingStatus.COMPLETED.equals(booking.getStatus())) {
            throw new IllegalArgumentException("Cannot confirm a cancelled or completed booking");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        return toResponse(bookingRepository.save(booking), null);
    }

    // ============================================================
    // PATCH /api/bookings/{id}/complete — Sitter จบงาน (COMPLETED)
    // ============================================================
    @Transactional
    public BookingResponse completeBooking(Long id, Long sitterId) {
        Bookings booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));

        if (!booking.getSitterId().equals(sitterId)) {
            throw new SecurityException("You are not authorized to complete this booking");
        }

        if (BookingStatus.CANCELLED.equals(booking.getStatus())) {
            throw new IllegalArgumentException("Cannot complete a cancelled booking");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return toResponse(bookingRepository.save(booking), null);
    }

    // ============================================================
    // Helper — แปลง Entity → BookingResponse DTO
    // ============================================================
    private BookingResponse toResponse(Bookings booking, String clientSecret) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setUserId(booking.getUserId());
        response.setPaymentMethod(booking.getPaymentMethod());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus().name());
        response.setPaymentIntentId(booking.getStripePaymentIntentId());
        response.setClientSecret(clientSecret);

        // Find Sitter Name (Priority: SitterProfile.tradeName -> User.email)
        String sitterName = sitterProfileRepository
                .findByUserId(booking.getSitterId())
                .map(sp -> sp.getTradeName())
                .filter(name -> name != null && !name.isEmpty())
                .orElseGet(() -> userRepository
                        .findById(booking.getSitterId())
                        .map(u -> u.getEmail())
                        .orElse("Unknown Sitter"));

        List<String> petNames = petRepository
                .findAllById(booking.getPetIds())
                .stream()
                .map(p -> p.getName())
                .collect(Collectors.toList());

        double totalHours = Duration.between(
                booking.getStartTime().atDate(booking.getStartDate()),
                booking.getEndTime().atDate(booking.getEndDate())).toMinutes() / 60.0;

        response.setSitterName(sitterName);
        response.setPetNames(petNames);
        response.setPetIds(booking.getPetIds());
        response.setStartDate(booking.getStartDate());
        response.setEndDate(booking.getEndDate());
        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());
        response.setTotalHours(totalHours);
        response.setPricePerHour(booking.getPricePerHour());
        response.setNoteToSitter(booking.getNoteToSitter());

        return response;
    }
}