package ru.mirea.sport.dto.bulk;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.mirea.sport.domain.BookingStatus;

import java.util.List;
import java.util.UUID;

public record BulkRequest(
    @NotEmpty List<UUID> ids,
    @NotNull BulkAction action,
    BookingStatus statusValue, // Используется для CHANGE_STATUS
    String tagValue            // Используется для ADD_TAG / REMOVE_TAG
) {}