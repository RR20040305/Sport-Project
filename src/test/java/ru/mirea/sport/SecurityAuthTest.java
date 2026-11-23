package ru.mirea.sport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.sport.dto.auth.LoginRequest;
import ru.mirea.sport.repository.UserRepository;
import ru.mirea.sport.service.AuthService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SecurityAuthTest {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;

    @Test
    void shouldLockAccountAfterFiveFailedAttempts() {
        // По условиям application-test.yml администратор admin@test.com
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> authService.login(new LoginRequest("admin@test.com", "wrong_pass")))
                    .hasMessageContaining("Неверный логин или пароль");
        }

        // 6-я попытка должна упасть из-за блокировки
        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@test.com", "wrong_pass")))
                .hasMessageContaining("Аккаунт заблокирован до");

        var user = userRepository.findByEmail("admin@test.com").orElseThrow();
        assertThat(user.getLockedUntil()).isNotNull();
    }
}