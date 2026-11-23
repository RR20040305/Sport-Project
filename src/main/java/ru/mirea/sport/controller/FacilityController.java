package ru.mirea.sport.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mirea.sport.dto.facility.FacilityRequest;
import ru.mirea.sport.dto.facility.FacilityResponse;
import ru.mirea.sport.service.FacilityService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/references")
public class FacilityController {
    private final FacilityService service;

    public FacilityController(FacilityService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<FacilityResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacilityResponse> create(@Valid @RequestBody FacilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacilityResponse> update(@PathVariable UUID id, @Valid @RequestBody FacilityRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleActive(@PathVariable UUID id) {
        service.toggleActive(id);
        return ResponseEntity.ok().build();
    }
}