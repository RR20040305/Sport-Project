package ru.mirea.sport.dto.auth;

import java.util.Set;
import java.util.UUID;

public record UserProfileDto(
    UUID id,
    String email,
    String fullName,
    Set<String> roles,
    boolean passwordChangeRequired
) {}