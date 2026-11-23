package ru.mirea.sport.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.mirea.sport.domain.RoleName;
import java.util.Set;

public record UserUpdateRequest(
    @NotBlank @Email String email,
    @NotBlank String fullName,
    @NotEmpty Set<RoleName> roles,
    @NotNull Boolean active
) {}