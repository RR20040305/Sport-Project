package ru.mirea.sport.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mirea.sport.dto.admin.TemporaryPasswordResponse;
import ru.mirea.sport.dto.admin.UserAdminResponse;
import ru.mirea.sport.dto.admin.UserCreateRequest;
import ru.mirea.sport.dto.admin.UserUpdateRequest;
import ru.mirea.sport.service.UserAdminService;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {
    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public ResponseEntity<Page<UserAdminResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userAdminService.getAll(pageable));
    }

    @PostMapping
    public ResponseEntity<TemporaryPasswordResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAdminService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAdminResponse> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userAdminService.update(id, request));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<TemporaryPasswordResponse> resetPassword(@PathVariable UUID id) {
        return ResponseEntity.ok(userAdminService.resetPassword(id));
    }
}