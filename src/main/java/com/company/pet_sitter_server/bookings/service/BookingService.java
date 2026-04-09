package com.company.pet_sitter_server.bookings.service;

import com.company.pet_sitter_server.bookings.dto.BookingRequest;
import com.company.pet_sitter_server.bookings.dto.BookingResponse;
import com.company.pet_sitter_server.bookings.dto.StripePaymentResponse;
import com.company.pet_sitter_server.bookings.entity.Bookings;
import com.company.pet_sitter_server.bookings.conflict.service.BookingConflictService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
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
    private final com.company.pet_sitter_server.user.repository.UserProfileRepository userProfileRepository;
    private final BookingConflictService bookingConflictService;
    private final ZoneId BANGKOK_ZONE = ZoneId.of("Asia/Bangkok");

    // ============================================================
    // POST /api/bookings — สร้าง Booking ใหม่
    // ============================================================
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // ดึงข้อมูล SitterProfile เพื่อดึง pricePerHour
        SitterProfile sitterProfile = sitterProfileRepository.findById(request.getSitterId())
                .orElseThrow(() -> new RuntimeException("Sitter Profile not found"));

        // [Double Check] ป้องกันการจองซ้อน
        validateNoOverlap(sitterProfile.getUser().getId(), 
                request.getStartDate(), request.getStartTime(),
                request.getEndDate(), request.getEndTime(), null);

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

        // Additive conflict check: prevent owner from creating a booking that overlaps
        // another active booking of the same sitter.
        bookingConflictService.ensureNoOverlapForSitter(
                booking.getSitterId(),
                booking.getStartDate(),
                booking.getStartTime(),
                booking.getEndDate(),
                booking.getEndTime(),
                null);

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

        List<Bookings> bookings = rawPage.getContent();
        autoCancelBatch(bookings);

        List<BookingResponse> content = buildOptimizedResponses(bookings);
        return new org.springframework.data.domain.PageImpl<>(content, pageable, rawPage.getTotalElements());
    }

    // ============================================================
    // GET /api/bookings/sitter/me — ดึงรายการจองที่ Sitter คนนี้ถูกจอง
    // ============================================================
    @Transactional
    public org.springframework.data.domain.Page<BookingResponse> getBookingsBySitter(Long sitterId, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Bookings> rawPage = bookingRepository.findBySitterIdOrderByCreatedAtDesc(sitterId, pageable);
        if (rawPage.isEmpty()) return org.springframework.data.domain.Page.empty();

        List<Bookings> bookings = rawPage.getContent();
        autoCancelBatch(bookings);

        List<BookingResponse> content = buildOptimizedResponses(bookings);
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

        // Additive conflict check: prevent sitter from confirming overlapping jobs.
        bookingConflictService.ensureNoOverlapForSitter(
                booking.getSitterId(),
                booking.getStartDate(),
                booking.getStartTime(),
                booking.getEndDate(),
                booking.getEndTime(),
                booking.getId());

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

        if (!BookingStatus.IN_SERVICE.equals(booking.getStatus())) {
            throw new IllegalArgumentException("Only in-service bookings can be completed.");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return toResponse(bookingRepository.save(booking), null);
    }

    // ============================================================
    // PATCH /api/bookings/{id}/start-service — Sitter เริ่มงาน (IN_SERVICE)
    // ============================================================
    @Transactional
    public BookingResponse startService(Long id, Long sitterId) {
        Bookings booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));

        if (!booking.getSitterId().equals(sitterId)) {
            throw new SecurityException("You are not authorized to start this booking");
        }

        if (!BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            throw new IllegalArgumentException("Only confirmed bookings can be started.");
        }

        booking.setStatus(BookingStatus.IN_SERVICE);
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

        if (BookingStatus.CONFIRMED.equals(booking.getStatus()) ||
                BookingStatus.IN_SERVICE.equals(booking.getStatus()) ||
                BookingStatus.COMPLETED.equals(booking.getStatus()) ||
                BookingStatus.CANCELLED.equals(booking.getStatus())) {
            throw new IllegalArgumentException("Cannot update datetime for a booking that is already confirmed or completed");
        }

        // ดึงเรทราคาปัจจุบันของ Sitter
        SitterProfile sitterProfile = sitterProfileRepository.findByUserId(booking.getSitterId())
                .orElseThrow(() -> new RuntimeException("Sitter Profile not found"));

        // [Double Check] ป้องกันการจองซ้อนตอนอัปเดต โดยเว้น ID ตัวเองไว้
        validateNoOverlap(booking.getSitterId(), 
                request.getStartDate(), request.getStartTime(),
                request.getEndDate(), request.getEndTime(), id);

        // คำนวณราคาใหม่
        double newTotalPrice = calculateTotalPrice(
                request.getStartDate(), request.getStartTime(),
                request.getEndDate(), request.getEndTime(),
                sitterProfile.getPricePerHour(),
                booking.getPetIds().size());

        // Additive conflict check: prevent changing time into an occupied slot.
        bookingConflictService.ensureNoOverlapForSitter(
                booking.getSitterId(),
                request.getStartDate(),
                request.getStartTime(),
                request.getEndDate(),
                request.getEndTime(),
                booking.getId());

        // อัปเดตข้อมูล
        booking.setStartDate(request.getStartDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndDate(request.getEndDate());
        booking.setEndTime(request.getEndTime());
        booking.setTotalPrice(newTotalPrice);

        return toResponse(bookingRepository.save(booking), null);
    }

    // ============================================================
    // Check Availability — สำหรับใช้เช็คก่อนไปหน้า Booking
    // ============================================================
    @Transactional(readOnly = true)
    public boolean isSitterAvailable(Long sitterId, LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime) {
        try {
            // รองรับทั้ง SitterProfile ID และ User ID
            SitterProfile sitterProfile = sitterProfileRepository.findById(sitterId)
                    .or(() -> sitterProfileRepository.findByUserId(sitterId))
                    .orElseThrow(() -> new RuntimeException("Sitter Profile not found"));

            validateNoOverlap(sitterProfile.getUser().getId(), startDate, startTime, endDate, endTime, null);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    void validateNoOverlap(Long sitterId, LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime, Long excludeBookingId) {
        LocalDateTime requestedStart = LocalDateTime.of(startDate, startTime);
        LocalDateTime requestedEnd = LocalDateTime.of(endDate, endTime);

        if (requestedEnd.isBefore(requestedStart) || requestedEnd.isEqual(requestedStart)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // ดึงรายการจองที่ยัง Active ของ Sitter
        List<BookingStatus> activeStatuses = Arrays.asList(
                BookingStatus.PENDING, 
                BookingStatus.PAID, 
                BookingStatus.CONFIRMED, 
                BookingStatus.IN_SERVICE
        );
        
        List<Bookings> existingBookings = bookingRepository.findBySitterIdAndStatusIn(sitterId, activeStatuses);

        for (Bookings b : existingBookings) {
            // ถ้าเป็นการอัปเดต ให้ข้ามรายการตัวเอง
            if (excludeBookingId != null && b.getId().equals(excludeBookingId)) continue;

            LocalDateTime existingStart = LocalDateTime.of(b.getStartDate(), b.getStartTime());
            LocalDateTime existingEnd = LocalDateTime.of(b.getEndDate(), b.getEndTime());

            // Overlap Logic: (StartA < EndB) AND (EndA > StartB)
            if (requestedStart.isBefore(existingEnd) && requestedEnd.isAfter(existingStart)) {
                throw new IllegalArgumentException("This sitter is already booked during the selected time period.");
            }
        }
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
    // ============================================================
    // Helper — Batch Auto-Cancel (แทนที่ N+1 save() ใน stream)
    // ============================================================
    private void autoCancelBatch(List<Bookings> bookings) {
        java.time.ZonedDateTime nowBangkok = java.time.ZonedDateTime.now(BANGKOK_ZONE);
        List<Bookings> toCancel = bookings.stream()
                .filter(b -> BookingStatus.PENDING.equals(b.getStatus()) || BookingStatus.PAID.equals(b.getStatus()))
                .filter(b -> {
                    try {
                        java.time.ZonedDateTime startBangkok = java.time.LocalDateTime
                                .of(b.getStartDate(), b.getStartTime()).atZone(BANGKOK_ZONE);
                        return nowBangkok.isAfter(startBangkok.minusHours(1));
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        if (!toCancel.isEmpty()) {
            toCancel.forEach(b -> b.setStatus(BookingStatus.CANCELLED));
            bookingRepository.saveAll(toCancel); // 1 query แทน N queries
        }
    }

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
        java.util.Set<Long> ownerIds = bookings.stream().map(Bookings::getUserId).collect(Collectors.toSet());
        java.util.Set<Long> allPetIds = bookings.stream().flatMap(b -> b.getPetIds().stream())
                .collect(Collectors.toSet());

        // Batch fetch sitter names, profile images, AND profile IDs in one query
        java.util.Map<Long, String> sitterNamesMap = new java.util.HashMap<>();
        java.util.Map<Long, String> sitterImagesMap = new java.util.HashMap<>();
        java.util.Map<Long, Long> sitterProfileIdsMap = new java.util.HashMap<>();
        sitterProfileRepository.findAllByUserIdIn(sitterIds).forEach(sp -> {
            Long uid = sp.getUser().getId();
            if (sp.getTradeName() != null && !sp.getTradeName().isEmpty()) {
                sitterNamesMap.put(uid, sp.getTradeName());
            }
            sitterImagesMap.put(uid, sp.getProfileImage());
            sitterProfileIdsMap.put(uid, sp.getId());
        });

        java.util.Set<Long> missingNameIds = sitterIds.stream()
                .filter(id -> !sitterNamesMap.containsKey(id))
                .collect(Collectors.toSet());

        if (!missingNameIds.isEmpty()) {
            userRepository.findAllByIdIn(missingNameIds).forEach(u -> {
                sitterNamesMap.put(u.getId(), u.getEmail());
            });
        }

        // Batch fetch owner names from UserProfile
        java.util.Map<Long, String> ownerNamesMap = new java.util.HashMap<>();
        userProfileRepository.findAllByUser_IdIn(ownerIds).forEach(up -> {
            Long uid = up.getUser().getId();
            if (up.getFullName() != null && !up.getFullName().isBlank()) {
                ownerNamesMap.put(uid, up.getFullName());
            }
        });
        // Fallback to email for owners without a profile or name
        java.util.Set<Long> missingOwnerIds = ownerIds.stream()
                .filter(id -> !ownerNamesMap.containsKey(id))
                .collect(Collectors.toSet());
        if (!missingOwnerIds.isEmpty()) {
            userRepository.findAllByIdIn(missingOwnerIds).forEach(u -> ownerNamesMap.put(u.getId(), u.getEmail()));
        }

        // [New Additive Logic] Batch fetch sitter full names from UserProfile
        java.util.Map<Long, String> sitterFullNamesMap = new java.util.HashMap<>();
        userProfileRepository.findAllByUser_IdIn(sitterIds).forEach(up -> {
            Long uid = up.getUser().getId();
            if (up.getFullName() != null && !up.getFullName().isBlank()) {
                sitterFullNamesMap.put(uid, up.getFullName());
            }
        });
        // Fallback to tradeName or email for sitters without a formal name
        sitterIds.forEach(id -> {
            if (!sitterFullNamesMap.containsKey(id)) {
                sitterFullNamesMap.put(id, sitterNamesMap.getOrDefault(id, "Sitter #" + id));
            }
        });

        java.util.Map<Long, String> petNamesMap = petRepository.findAllById(allPetIds).stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> p.getName(), (existing, replacement) -> existing));

        java.util.Set<Long> bookingIds = bookings.stream().map(Bookings::getId).collect(Collectors.toSet());
        java.util.Map<Long, Long> reviewIdsMap = reviewRepository.findAllByBookingIdIn(bookingIds).stream()
                .collect(Collectors.toMap(Review::getBookingId, Review::getId));

        return bookings.stream()
                .map(b -> toResponseOptimized(b, sitterNamesMap, petNamesMap, reviewIdsMap, sitterImagesMap, ownerNamesMap, sitterFullNamesMap, sitterProfileIdsMap))
                .collect(Collectors.toList());
    }

    private BookingResponse toResponseOptimized(Bookings booking, java.util.Map<Long, String> sitterNamesMap,
            java.util.Map<Long, String> petNamesMap, java.util.Map<Long, Long> reviewIdsMap,
            java.util.Map<Long, String> sitterImagesMap, java.util.Map<Long, String> ownerNamesMap,
            java.util.Map<Long, String> sitterFullNamesMap, java.util.Map<Long, Long> sitterProfileIdsMap) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setUserId(booking.getUserId());
        response.setOwnerName(ownerNamesMap.getOrDefault(booking.getUserId(), "User #" + booking.getUserId()));
        response.setSitterId(booking.getSitterId());
        response.setSitterFullName(sitterFullNamesMap.getOrDefault(booking.getSitterId(), "Sitter #" + booking.getSitterId()));
        response.setSitterProfileId(sitterProfileIdsMap.get(booking.getSitterId()));
        response.setPaymentMethod(booking.getPaymentMethod());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus().name());
        response.setCreatedAt(booking.getCreatedAt());
        response.setPaymentIntentId(booking.getStripePaymentIntentId());
        response.setClientSecret(null);
        response.setReviewId(reviewIdsMap.get(booking.getId()));

        String sitterName = sitterNamesMap.getOrDefault(booking.getSitterId(), "Unknown Sitter");
        // ใช้จาก sitterImagesMap — ไม่ต้อง query เพิ่มเติม
        String sitterProfileImage = sitterImagesMap.get(booking.getSitterId());

        List<String> petNames = booking.getPetIds().stream()
                .map(id -> petNamesMap.getOrDefault(id, "Unknown Pet"))
                .collect(Collectors.toList());

        double totalHours = java.time.Duration.between(
                booking.getStartTime().atDate(booking.getStartDate()),
                booking.getEndTime().atDate(booking.getEndDate())).toMinutes() / 60.0;

        response.setSitterName(sitterName);
        response.setSitterProfileImage(sitterProfileImage);
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

        // Single booking — fetch sitterImage directly (acceptable for single call)
        java.util.Map<Long, String> sitterImagesMap = new java.util.HashMap<>();
        sitterProfileRepository.findByUserId(booking.getSitterId())
                .ifPresent(sp -> sitterImagesMap.put(booking.getSitterId(), sp.getProfileImage()));

        // Fetch owner name for single booking
        java.util.Map<Long, String> ownerNamesMap = new java.util.HashMap<>();
        userProfileRepository.findByUser_Id(booking.getUserId()).ifPresent(up -> {
            if (up.getFullName() != null && !up.getFullName().isBlank()) {
                ownerNamesMap.put(booking.getUserId(), up.getFullName());
            }
        });
        if (!ownerNamesMap.containsKey(booking.getUserId())) {
            userRepository.findById(booking.getUserId())
                    .ifPresent(u -> ownerNamesMap.put(u.getId(), u.getEmail()));
        }

        // [New Additive Logic] Fetch sitter full name for single booking
        java.util.Map<Long, String> sitterFullNamesMap = new java.util.HashMap<>();
        userProfileRepository.findByUser_Id(booking.getSitterId()).ifPresent(up -> {
            if (up.getFullName() != null && !up.getFullName().isBlank()) {
                sitterFullNamesMap.put(booking.getSitterId(), up.getFullName());
            }
        });
        // [New Additive Logic] Fetch sitter profile ID for fallback if needed
        java.util.Map<Long, Long> sitterProfileIdsMap = new java.util.HashMap<>();
        sitterProfileRepository.findByUserId(booking.getSitterId())
                .ifPresent(sp -> sitterProfileIdsMap.put(booking.getSitterId(), sp.getId()));

        if (!sitterFullNamesMap.containsKey(booking.getSitterId())) {
            String fallback = sitterProfileRepository.findByUserId(booking.getSitterId())
                    .map(sp -> sp.getTradeName())
                    .orElseGet(() -> userRepository.findById(booking.getSitterId()).map(u -> u.getEmail()).orElse("Sitter #" + booking.getSitterId()));
            sitterFullNamesMap.put(booking.getSitterId(), fallback);
        }

        BookingResponse res = toResponseOptimized(booking, new java.util.HashMap<>(), new java.util.HashMap<>(), reviewIdsMap, sitterImagesMap, ownerNamesMap, sitterFullNamesMap, sitterProfileIdsMap);
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