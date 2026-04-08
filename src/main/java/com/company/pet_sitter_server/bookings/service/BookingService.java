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
import com.company.pet_sitter_server.reviews.repository.ReviewRepository;
import com.company.pet_sitter_server.reviews.entity.Review;

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
    private final ReviewRepository reviewRepository;
    private final ZoneId BANGKOK_ZONE = ZoneId.of("Asia/Bangkok");

    // ============================================================
    // POST /api/bookings — สร้าง Booking ใหม่
    // ============================================================
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // ดึงข้อมูล SitterProfile เพื่อดึง pricePerHour
        SitterProfile sitterProfile = sitterProfileRepository.findById(request.getSitterId())
                .orElseThrow(() -> new RuntimeException("Sitter Profile not found"));

        // คำนวณราคาผ่าน helper
        double totalPrice = calculateTotalPrice(
                request.getStartDate(), request.getStartTime(),
                request.getEndDate(), request.getEndTime(),
                sitterProfile.getPricePerHour(),
                request.getPetIds().size());

        // แมพข้อมูลลง Entity
        Bookings booking = new Bookings();
        booking.setUserId(request.getUserId());
        booking.setSitterId(sitterProfile.getUser().getId());
        booking.setPetIds(request.getPetIds());

        Double pricePerHour = (sitterProfile.getPricePerHour() == null || sitterProfile.getPricePerHour() <= 0) ? 200.0
                : sitterProfile.getPricePerHour();
        booking.setPricePerHour(pricePerHour);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setTotalPrice(totalPrice);
        booking.setNoteToSitter(request.getNoteToSitter());

        // Normalize input string
        String method = (request.getPaymentMethod() != null) ? request.getPaymentMethod().trim() : "";
        booking.setPaymentMethod(method.toUpperCase());
        // All bookings start as PENDING (Card will wait for Webhook to become PAID)
        booking.setStatus(BookingStatus.PENDING);

        // สำหรับ CREDIT_CARD: สร้าง PaymentIntent ที่ Stripe
        String clientSecret = null;
        if ("CREDIT_CARD".equalsIgnoreCase(method)) {
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
    @Transactional
    public BookingResponse getBookingById(Long id) {
        Bookings booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));
        return toResponse(checkAndAutoCancel(booking), null);
    }

    // ============================================================
    // GET /api/bookings/user/{userId} — ดึง Booking history ของ user
    // ============================================================
    @Transactional
    public org.springframework.data.domain.Page<BookingResponse> getBookingsByUser(Long userId, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Bookings> rawPage = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        if (rawPage.isEmpty()) return org.springframework.data.domain.Page.empty();

        List<Bookings> checkedBookings = rawPage.stream()
                .map(this::checkAndAutoCancel)
                .collect(Collectors.toList());

        List<BookingResponse> content = buildOptimizedResponses(checkedBookings);
        return new org.springframework.data.domain.PageImpl<>(content, pageable, rawPage.getTotalElements());
    }

    // ============================================================
    // GET /api/bookings/sitter/me — ดึงรายการจองที่ Sitter คนนี้ถูกจอง
    // ============================================================
    @Transactional
    public org.springframework.data.domain.Page<BookingResponse> getBookingsBySitter(Long sitterId, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Bookings> rawPage = bookingRepository.findBySitterIdOrderByCreatedAtDesc(sitterId, pageable);
        if (rawPage.isEmpty()) return org.springframework.data.domain.Page.empty();

        List<Bookings> checkedBookings = rawPage.stream()
                .map(this::checkAndAutoCancel)
                .collect(Collectors.toList());

        List<BookingResponse> content = buildOptimizedResponses(checkedBookings);
        return new org.springframework.data.domain.PageImpl<>(content, pageable, rawPage.getTotalElements());
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

        // Sitter confirms job
        // Card: must be PAID (via Webhook)
        // Cash: starts as PENDING
        boolean isCardPaid = "CREDIT_CARD".equalsIgnoreCase(booking.getPaymentMethod())
                && BookingStatus.PAID.equals(booking.getStatus());
        boolean isCashPending = "CASH".equalsIgnoreCase(booking.getPaymentMethod())
                && BookingStatus.PENDING.equals(booking.getStatus());

        if (!isCardPaid && !isCashPending) {
            throw new IllegalArgumentException("Cannot confirm: Job is either unpaid (Card) or in an invalid state.");
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

        if (!BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            throw new IllegalArgumentException("Only confirmed bookings can be completed.");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return toResponse(bookingRepository.save(booking), null);
    }

    // ============================================================
    // PATCH /api/bookings/{id}/verify-payment — ตรวจสอบผลการชำระเงินกับ Stripe
    // ============================================================
    @Transactional
    public BookingResponse verifyPayment(Long id) {
        Bookings booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));

        if (!"CREDIT_CARD".equalsIgnoreCase(booking.getPaymentMethod())) {
            throw new IllegalArgumentException("This booking is not a credit card payment.");
        }

        // If already paid, just return it
        if (BookingStatus.PAID.equals(booking.getStatus()) || BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            return toResponse(booking, null);
        }

        String piId = booking.getStripePaymentIntentId();
        if (piId == null || piId.isEmpty()) {
            throw new RuntimeException("No Stripe Payment Intent ID found for this booking.");
        }

        try {
            com.stripe.model.PaymentIntent intent = stripeService.retrievePaymentIntent(piId);
            if ("succeeded".equalsIgnoreCase(intent.getStatus())) {
                booking.setStatus(BookingStatus.PAID);
                return toResponse(bookingRepository.save(booking), null);
            } else {
                throw new RuntimeException("Payment Status at Stripe is: " + intent.getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify with Stripe: " + e.getMessage());
        }
    }

    // ============================================================
    // PATCH /api/bookings/{id}/datetime — อัปเดตวันเวลาจอง
    // ============================================================
    @Transactional
    public BookingResponse updateBookingDateTime(Long id, BookingRequest request) {
        Bookings booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));

        if (BookingStatus.COMPLETED.equals(booking.getStatus()) ||
                BookingStatus.CANCELLED.equals(booking.getStatus())) {
            throw new IllegalArgumentException("Cannot update time for a completed or cancelled booking");
        }

        // ดึงเรทราคาปัจจุบันของ Sitter
        SitterProfile sitterProfile = sitterProfileRepository.findByUserId(booking.getSitterId())
                .orElseThrow(() -> new RuntimeException("Sitter Profile not found"));

        // คำนวณราคาใหม่
        double newTotalPrice = calculateTotalPrice(
                request.getStartDate(), request.getStartTime(),
                request.getEndDate(), request.getEndTime(),
                sitterProfile.getPricePerHour(),
                booking.getPetIds().size());

        // อัปเดตข้อมูล
        booking.setStartDate(request.getStartDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndDate(request.getEndDate());
        booking.setEndTime(request.getEndTime());
        booking.setTotalPrice(newTotalPrice);

        return toResponse(bookingRepository.save(booking), null);
    }

    // ============================================================
    // Helper — คำนวณราคาตามชั่วโมงและจำนวนสัตว์เลี้ยง
    // ============================================================
    private double calculateTotalPrice(java.time.LocalDate startDate, java.time.LocalTime startTime,
            java.time.LocalDate endDate, java.time.LocalTime endTime,
            Double sitterPricePerHour, int numPets) {

        ZonedDateTime startBangkok = ZonedDateTime.of(startDate, startTime, BANGKOK_ZONE);
        ZonedDateTime endBangkok = ZonedDateTime.of(endDate, endTime, BANGKOK_ZONE);

        if (endBangkok.isBefore(startBangkok)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        long minutes = Duration.between(startBangkok, endBangkok).toMinutes();
        double hours = minutes / 60.0;

        Double pricePerHour = (sitterPricePerHour == null || sitterPricePerHour <= 0) ? 200.0 : sitterPricePerHour;

        // สูตรเดิม: (ราคาต่อชม * ชม) + (100 * ชม * สัตว์เลี้ยงตัวที่เพิ่มมา)
        return (pricePerHour * hours) + (100 * hours * (numPets - 1));
    }

    // ============================================================
    // Helper — แปลง Entity → BookingResponse DTO
    // ============================================================
    private Bookings checkAndAutoCancel(Bookings booking) {
        if (BookingStatus.PENDING.equals(booking.getStatus()) || BookingStatus.PAID.equals(booking.getStatus())) {
            try {
                java.time.LocalDateTime startDateTime = java.time.LocalDateTime.of(booking.getStartDate(),
                        booking.getStartTime());
                java.time.ZonedDateTime nowBangkok = java.time.ZonedDateTime.now(BANGKOK_ZONE);
                java.time.ZonedDateTime startBangkok = startDateTime.atZone(BANGKOK_ZONE);

                if (nowBangkok.isAfter(startBangkok.minusHours(1))) {
                    booking.setStatus(BookingStatus.CANCELLED);
                    return bookingRepository.save(booking);
                }
            } catch (Exception e) {
                System.err.println("Auto-cancel check failed for booking " + booking.getId() + ": " + e.getMessage());
            }
        }
        return booking;
    }

    private List<BookingResponse> buildOptimizedResponses(List<Bookings> bookings) {
        java.util.Set<Long> sitterIds = bookings.stream().map(Bookings::getSitterId).collect(Collectors.toSet());
        java.util.Set<Long> allPetIds = bookings.stream().flatMap(b -> b.getPetIds().stream())
                .collect(Collectors.toSet());

        java.util.Map<Long, String> sitterNamesMap = new java.util.HashMap<>();
        sitterProfileRepository.findAllByUserIdIn(sitterIds).forEach(sp -> {
            if (sp.getTradeName() != null && !sp.getTradeName().isEmpty()) {
                sitterNamesMap.put(sp.getUser().getId(), sp.getTradeName());
            }
        });

        java.util.Set<Long> missingNameIds = sitterIds.stream()
                .filter(id -> !sitterNamesMap.containsKey(id))
                .collect(Collectors.toSet());

        if (!missingNameIds.isEmpty()) {
            userRepository.findAllByIdIn(missingNameIds).forEach(u -> {
                sitterNamesMap.put(u.getId(), u.getEmail());
            });
        }

        java.util.Map<Long, String> petNamesMap = petRepository.findAllById(allPetIds).stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> p.getName(), (existing, replacement) -> existing));

        java.util.Set<Long> bookingIds = bookings.stream().map(Bookings::getId).collect(Collectors.toSet());
        java.util.Map<Long, Long> reviewIdsMap = reviewRepository.findAllByBookingIdIn(bookingIds).stream()
                .collect(Collectors.toMap(Review::getBookingId, Review::getId));

        return bookings.stream()
                .map(b -> toResponseOptimized(b, sitterNamesMap, petNamesMap, reviewIdsMap))
                .collect(Collectors.toList());
    }

    private BookingResponse toResponseOptimized(Bookings booking, java.util.Map<Long, String> sitterNamesMap,
            java.util.Map<Long, String> petNamesMap, java.util.Map<Long, Long> reviewIdsMap) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setUserId(booking.getUserId());
        response.setPaymentMethod(booking.getPaymentMethod());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus().name());
        response.setPaymentIntentId(booking.getStripePaymentIntentId());
        response.setClientSecret(null);
        response.setReviewId(reviewIdsMap.get(booking.getId()));

        String sitterName = sitterNamesMap.getOrDefault(booking.getSitterId(), "Unknown Sitter");

        List<String> petNames = booking.getPetIds().stream()
                .map(id -> petNamesMap.getOrDefault(id, "Unknown Pet"))
                .collect(Collectors.toList());

        double totalHours = java.time.Duration.between(
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

    private BookingResponse toResponse(Bookings booking, String clientSecret) {
        java.util.Map<Long, Long> reviewIdsMap = new java.util.HashMap<>();
        reviewRepository.findByBookingId(booking.getId()).ifPresent(r -> reviewIdsMap.put(booking.getId(), r.getId()));

        BookingResponse res = toResponseOptimized(booking, new java.util.HashMap<>(), new java.util.HashMap<>(), reviewIdsMap);
        res.setClientSecret(clientSecret);

        if ("Unknown Sitter".equals(res.getSitterName())) {
            String name = sitterProfileRepository.findByUserId(booking.getSitterId())
                    .map(sp -> sp.getTradeName())
                    .filter(n -> n != null && !n.isEmpty())
                    .orElseGet(() -> userRepository.findById(booking.getSitterId())
                            .map(u -> u.getEmail()).orElse("Unknown Sitter"));
            res.setSitterName(name);
        }

        if (res.getPetNames() == null || res.getPetNames().isEmpty() || res.getPetNames().contains("Unknown Pet")) {
            res.setPetNames(petRepository.findAllById(booking.getPetIds()).stream().map(p -> p.getName())
                    .collect(Collectors.toList()));
        }

        return res;
    }
}