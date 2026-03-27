package com.company.pet_sitter_server.dto.booking;

import com.company.pet_sitter_server.dto.pet.PetResponse;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class BookingResponse {
    private Long id;
    private Long userId;
    private String ownerName;
    private Long sitterId;
    private String sitterTradeName;
    private Long sitterServiceId;
    private String serviceName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private Double totalPrice;
    private String noteToSitter;
    private List<PetResponse> pets;
    private OffsetDateTime createdAt;
}
