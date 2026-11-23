package ru.mirea.sport.dto.auth;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    boolean passwordChangeRequired
) {}