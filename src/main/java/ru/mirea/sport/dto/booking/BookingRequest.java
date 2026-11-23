package ru.mirea.sport.dto.booking;

import jakarta.validation.constraints.*;
import ru.mirea.sport.domain.PriorityLevel;
import ru.mirea.sport.domain.BookingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingRequest(
    @NotNull UUID facilityId,
    @NotBlank String clientName,
    @NotBlank String clientPhone,
    @NotNull LocalDateTime startTime,
    @NotNull LocalDateTime endTime,
    @NotNull BookingStatus status,
    @NotNull Boolean isPaid,
    @NotNull PriorityLevel priorityLevel,
    Long version
) {}