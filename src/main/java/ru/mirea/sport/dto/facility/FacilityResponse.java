package ru.mirea.sport.dto.facility;

import java.math.BigDecimal;
import java.util.UUID;

public record FacilityResponse(
    UUID id,
    String name,
    String description,
    boolean active,
    BigDecimal pricePerHour
) {}