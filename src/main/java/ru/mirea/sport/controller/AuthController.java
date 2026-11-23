package ru.mirea.sport.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.mirea.sport.domain.User;
import ru.mirea.sport.dto.auth.*;
import ru.mirea.sport.repository.UserRepository;
import ru.mirea.sport.security.CustomUserDetails;
import ru.mirea.sport.service.AuditService;
import ru.mirea.sport.service.AuthService;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AuthController(AuthService authService, UserRepository userRepository, AuditService auditService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request);
        String correlationId = (String) httpRequest.getAttribute("correlationId");
        auditService.log("LOGIN", "User", null, "Успешный вход пользователя " + request.email(), correlationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();

        return ResponseEntity.ok(new UserProfileDto(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()),
            user.isPasswordChangeRequired()
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails, request);
        return ResponseEntity.ok().build();
    }
}