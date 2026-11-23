package ru.mirea.sport.dto.analytics;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsResponse(
    long totalCount,
    long activeCount,
    long archivedCount,
    long paidCount,                  // Количество по логическому признаку
    BigDecimal averagePrice,         // Среднее значение числового показателя
    List<GroupCountDto> byFacility,  // Группировка по справочнику
    List<GroupCountDto> byStatus,    // Группировка по статусу
    List<GroupCountDto> byPriority   // Группировка по вычисляемому показателю
) {}