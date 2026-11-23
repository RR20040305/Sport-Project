package ru.mirea.sport.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.sport.domain.Role;
import ru.mirea.sport.domain.RoleName;
import ru.mirea.sport.domain.User;
import ru.mirea.sport.dto.admin.TemporaryPasswordResponse;
import ru.mirea.sport.dto.admin.UserAdminResponse;
import ru.mirea.sport.dto.admin.UserCreateRequest;
import ru.mirea.sport.dto.admin.UserUpdateRequest;
import ru.mirea.sport.repository.RoleRepository;
import ru.mirea.sport.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserAdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserAdminResponse> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional
    public TemporaryPasswordResponse create(UserCreateRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        String tempPassword = UUID.randomUUID().toString().substring(0, 8) + "A1!";

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setActive(true);
        user.setPasswordChangeRequired(true);
        user.setRoles(resolveRoles(request.roles()));

        userRepository.save(user);
        return new TemporaryPasswordResponse(user.getEmail(), tempPassword);
    }

    @Transactional
    public UserAdminResponse update(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (!user.getEmail().equals(request.email()) && userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setActive(request.active());
        user.setRoles(resolveRoles(request.roles()));

        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public TemporaryPasswordResponse resetPassword(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        String tempPassword = UUID.randomUUID().toString().substring(0, 8) + "A1!";
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setPasswordChangeRequired(true);
        user.setFailedAttempts(0);
        user.setLockedUntil(null);

        userRepository.save(user);
        return new TemporaryPasswordResponse(user.getEmail(), tempPassword);
    }

    private Set<Role> resolveRoles(Set<RoleName> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (RoleName name : roleNames) {
            Role role = roleRepository.findByName(name)
                    .orElseThrow(() -> new RuntimeException("Роль не найдена: " + name));
            roles.add(role);
        }
        return roles;
    }

    private UserAdminResponse mapToDto(User user) {
        return new UserAdminResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
            user.isActive(),
            user.isPasswordChangeRequired()
        );
    }
}