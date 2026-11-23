package ru.mirea.sport.dto.audit;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditEventResponse(
    UUID id,
    String userName,
    String eventType,
    String entityType,
    UUID entityId,
    String correlationId,
    LocalDateTime createdAt,
    String description
) {}