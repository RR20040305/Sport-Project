package ru.mirea.sport.dto.facility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record FacilityRequest(
    @NotBlank @Size(max = 255) String name,
    String description,
    Boolean active,
    @NotNull BigDecimal pricePerHour
) {}