package ru.mirea.sport.dto.csv;

import java.util.List;

public record CsvPreviewResponse(
    int validCount,
    int errorCount,
    List<String> errors
) {}