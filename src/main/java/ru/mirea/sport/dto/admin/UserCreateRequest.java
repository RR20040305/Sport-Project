package ru.mirea.sport.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import ru.mirea.sport.domain.RoleName;
import java.util.Set;

public record UserCreateRequest(
    @NotBlank @Email String email,
    @NotBlank String fullName,
    @NotEmpty Set<RoleName> roles
) {}