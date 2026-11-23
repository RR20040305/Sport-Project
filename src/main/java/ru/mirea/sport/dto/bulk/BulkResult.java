package ru.mirea.sport.dto.bulk;

import java.util.List;

public record BulkResult(
    int successCount,
    int errorCount,
    List<String> errors
) {}