package com.company.pet_sitter_server.bookings.calendar.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.company.pet_sitter_server.bookings.calendar.dto.SitterCalendarItemResponse;
import com.company.pet_sitter_server.bookings.calendar.repository.SitterCalendarBookingRepository;
import com.company.pet_sitter_server.bookings.entity.Bookings;
import com.company.pet_sitter_server.enums.BookingStatus;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.entity.UserProfile;
import com.company.pet_sitter_server.user.repository.UserProfileRepository;
import com.company.pet_sitter_server.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SitterCalendarService {
    private final SitterCalendarBookingRepository calendarBookingRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public List<SitterCalendarItemResponse> getMyCalendar(Long sitterId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate are required.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be on or after startDate.");
        }

        List<Bookings> bookings = calendarBookingRepository.findCalendarBookingsBySitterAndDateRange(
                sitterId,
                startDate,
                endDate,
                BookingStatus.CANCELLED);

        Set<Long> ownerIds = bookings.stream().map(Bookings::getUserId).collect(Collectors.toSet());
        Map<Long, String> ownerNameMap = buildOwnerNameMap(ownerIds);

        return bookings.stream()
                .map(booking -> toResponse(booking, ownerNameMap.getOrDefault(booking.getUserId(), "Owner")))
                .collect(Collectors.toList());
    }

    private Map<Long, String> buildOwnerNameMap(Set<Long> ownerIds) {
        if (ownerIds.isEmpty()) return Map.of();

        Map<Long, String> result = new java.util.HashMap<>();

        for (Long userId : ownerIds) {
            String fullName = userProfileRepository.findByUserId(userId)
                    .map(UserProfile::getFullName)
                    .filter(name -> name != null && !name.isBlank())
                    .orElse(null);
            if (fullName != null) {
                result.put(userId, fullName);
            }
        }

        Set<Long> missingIds = ownerIds.stream().filter(id -> !result.containsKey(id)).collect(Collectors.toSet());
        if (!missingIds.isEmpty()) {
            userRepository.findAllByIdIn(missingIds).forEach(user -> {
                result.put(user.getId(), fallbackOwnerName(user));
            });
        }

        return result;
    }

    private String fallbackOwnerName(User user) {
        if (user == null) return "Owner";
        if (user.getEmail() == null || user.getEmail().isBlank()) return "Owner";
        return user.getEmail();
    }

    private SitterCalendarItemResponse toResponse(Bookings booking, String ownerName) {
        SitterCalendarItemResponse response = new SitterCalendarItemResponse();
        response.setBookingId(booking.getId());
        response.setOwnerId(booking.getUserId());
        response.setOwnerName(ownerName);
        response.setStartDate(booking.getStartDate());
        response.setStartTime(booking.getStartTime());
        response.setEndDate(booking.getEndDate());
        response.setEndTime(booking.getEndTime());
        response.setStatus(booking.getStatus().name());
        return response;
    }
}
