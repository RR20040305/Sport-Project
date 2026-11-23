package ru.mirea.sport.dto.booking;

import ru.mirea.sport.domain.BookingStatus;
import ru.mirea.sport.domain.PriorityLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    String bookingCode,
    String facilityName,
    String clientName,
    String clientPhone,
    LocalDateTime startTime,
    LocalDateTime endTime,
    BookingStatus status,
    BigDecimal totalPrice,
    Boolean isPaid,
    PriorityLevel priorityLevel,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long version,
    Boolean archived
) {}