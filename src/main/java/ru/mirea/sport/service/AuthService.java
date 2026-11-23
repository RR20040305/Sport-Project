package ru.mirea.sport.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.sport.domain.RefreshSession;
import ru.mirea.sport.domain.User;
import ru.mirea.sport.dto.auth.*;
import ru.mirea.sport.repository.RefreshSessionRepository;
import ru.mirea.sport.repository.UserRepository;
import ru.mirea.sport.security.CustomUserDetails;
import ru.mirea.sport.security.JwtProperties;
import ru.mirea.sport.security.JwtService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Константы для защиты от брутфорса
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 15;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       JwtProperties jwtProperties, RefreshSessionRepository sessionRepository,
                       UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Неверный логин или пароль"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Аккаунт заблокирован до " + user.getLockedUntil());
        }

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            // Успешный вход: сбрасываем счетчик
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);

            return generateTokens(user);
        } catch (Exception e) {
            // Неудачный вход: инкрементируем счетчик
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_TIME_MINUTES));
            }
            userRepository.save(user);
            throw new RuntimeException("Неверный логин или пароль");
        }
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshSession session = sessionRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Недействительный refresh token"));

        if (session.isRevoked() || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Сессия истекла или отозвана");
        }

        User user = session.getUser();
        String newAccessToken = jwtService.generateAccessToken(session.getUser());
        return new AuthResponse(newAccessToken, refreshToken, user.isPasswordChangeRequired());
    }

    @Transactional
    public void logout(String refreshToken) {
        sessionRepository.findByToken(refreshToken).ifPresent(session -> {
            session.setRevoked(true);
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void changePassword(CustomUserDetails userDetails, ChangePasswordRequest request) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Старый пароль неверен");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangeRequired(false);
        userRepository.save(user);
    }

    private AuthResponse generateTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken();

        RefreshSession session = new RefreshSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(jwtProperties.refreshDays()));
        session.setRevoked(false);
        sessionRepository.save(session);

        return new AuthResponse(accessToken, refreshToken, user.isPasswordChangeRequired());
    }
}