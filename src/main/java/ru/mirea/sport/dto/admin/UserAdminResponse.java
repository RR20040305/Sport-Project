package ru.mirea.sport.dto.admin;

import ru.mirea.sport.domain.RoleName;
import java.util.Set;
import java.util.UUID;

public record UserAdminResponse(
    UUID id,
    String email,
    String fullName,
    Set<RoleName> roles,
    boolean active,
    boolean passwordChangeRequired
) {}