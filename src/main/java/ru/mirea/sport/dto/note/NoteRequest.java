package ru.mirea.sport.dto.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NoteRequest(
    @NotBlank String content,
    @NotNull Boolean pinned
) {}