package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.dto.booking.BookingRequest;
import com.company.pet_sitter_server.dto.booking.BookingResponse;
import com.company.pet_sitter_server.dto.pet.PetResponse;
import com.company.pet_sitter_server.entity.*;
import com.company.pet_sitter_server.enums.BookingStatus;
import com.company.pet_sitter_server.exception.AccessDeniedException;
import com.company.pet_sitter_server.exception.BadRequestException;
import com.company.pet_sitter_server.exception.NotFoundException;
import com.company.pet_sitter_server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private BookingPetRepository bookingPetRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private PetService petService;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private SitterProfileRepository sitterProfileRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private SitterOfferRepository sitterOfferRepository;

    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest request) {
        if (request.getPetIds() == null || request.getPetIds().isEmpty()) {
            throw new BadRequestException("At least one pet must be selected");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setSitterId(request.getSitterId());
        booking.setSitterServiceId(request.getSitterServiceId());
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setNoteToSitter(request.getNoteToSitter());
        booking.setStatus(BookingStatus.WAITING_FOR_CONFIRM);

        sitterOfferRepository.findById(request.getSitterServiceId()).ifPresent(offer ->
                booking.setTotalPrice(offer.getPricePerHour()));

        Booking saved = bookingRepository.save(booking);

        for (Long petId : request.getPetIds()) {
            petRepository.findById(petId).orElseThrow(() ->
                    new NotFoundException("Pet not found with id: " + petId));
            BookingPet bp = new BookingPet();
            bp.setBookingId(saved.getId());
            bp.setPetId(petId);
            bookingPetRepository.save(bp);
        }

        return toResponse(saved);
    }

    public Page<BookingResponse> getMyBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public BookingResponse getBookingById(Long bookingId, Long userId) {
        Booking booking = findById(bookingId);
        if (!booking.getUserId().equals(userId) && !booking.getSitterId().equals(userId)) {
            throw new AccessDeniedException("You do not have access to this booking");
        }
        return toResponse(booking);
    }

    public BookingResponse cancelBooking(Long bookingId, Long userId) {
        Booking booking = findById(bookingId);
        if (!booking.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not own this booking");
        }
        if (booking.getStatus() == BookingStatus.SUCCESS || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a booking with status: " + booking.getStatus());
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    public Page<BookingResponse> getSitterBookings(Long sitterId, Pageable pageable) {
        return bookingRepository.findBySitterIdOrderByCreatedAtDesc(sitterId, pageable)
                .map(this::toResponse);
    }

    public BookingResponse confirmBooking(Long bookingId, Long sitterId) {
        return updateSitterStatus(bookingId, sitterId, BookingStatus.WAITING_FOR_SERVICE,
                BookingStatus.WAITING_FOR_CONFIRM);
    }

    public BookingResponse rejectBooking(Long bookingId, Long sitterId) {
        return updateSitterStatus(bookingId, sitterId, BookingStatus.CANCELLED,
                BookingStatus.WAITING_FOR_CONFIRM);
    }

    public BookingResponse startService(Long bookingId, Long sitterId) {
        return updateSitterStatus(bookingId, sitterId, BookingStatus.IN_SERVICE,
                BookingStatus.WAITING_FOR_SERVICE);
    }

    public BookingResponse completeService(Long bookingId, Long sitterId) {
        return updateSitterStatus(bookingId, sitterId, BookingStatus.SUCCESS,
                BookingStatus.IN_SERVICE);
    }

    private BookingResponse updateSitterStatus(Long bookingId, Long sitterId,
                                                BookingStatus newStatus, BookingStatus requiredCurrent) {
        Booking booking = findById(bookingId);
        if (!booking.getSitterId().equals(sitterId)) {
            throw new AccessDeniedException("This booking does not belong to you");
        }
        if (booking.getStatus() != requiredCurrent) {
            throw new BadRequestException("Cannot transition from " + booking.getStatus() + " to " + newStatus);
        }
        booking.setStatus(newStatus);
        return toResponse(bookingRepository.save(booking));
    }

    private Booking findById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));
    }

    public BookingResponse toResponse(Booking booking) {
        BookingResponse r = new BookingResponse();
        r.setId(booking.getId());
        r.setUserId(booking.getUserId());
        r.setSitterId(booking.getSitterId());
        r.setSitterServiceId(booking.getSitterServiceId());
        r.setStartDate(booking.getStartDate());
        r.setEndDate(booking.getEndDate());
        r.setStartTime(booking.getStartTime());
        r.setEndTime(booking.getEndTime());
        r.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        r.setTotalPrice(booking.getTotalPrice());
        r.setNoteToSitter(booking.getNoteToSitter());
        r.setCreatedAt(booking.getCreatedAt());

        userProfileRepository.findByUserId(booking.getUserId())
                .ifPresent(up -> r.setOwnerName(up.getFullName()));

        sitterProfileRepository.findByUserId(booking.getSitterId())
                .ifPresent(sp -> r.setSitterTradeName(sp.getTradeName()));

        if (booking.getSitterServiceId() != null) {
            sitterOfferRepository.findById(booking.getSitterServiceId()).ifPresent(offer ->
                    serviceRepository.findById(offer.getServiceId())
                            .ifPresent(s -> r.setServiceName(s.getName())));
        }

        List<PetResponse> pets = bookingPetRepository.findByBookingId(booking.getId()).stream()
                .map(bp -> petRepository.findById(bp.getPetId()).map(petService::toResponse).orElse(null))
                .filter(p -> p != null)
                .toList();
        r.setPets(pets);

        return r;
    }
}
