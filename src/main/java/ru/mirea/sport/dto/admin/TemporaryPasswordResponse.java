package ru.mirea.sport.dto.admin;

public record TemporaryPasswordResponse(
    String email,
    String temporaryPassword
) {}