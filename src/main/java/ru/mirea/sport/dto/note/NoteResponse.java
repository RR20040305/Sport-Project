package ru.mirea.sport.dto.note;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoteResponse(
    UUID id,
    String content,
    boolean pinned,
    String authorName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}