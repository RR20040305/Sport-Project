package ru.mirea.sport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.sport.domain.RefreshSession;
import ru.mirea.sport.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, UUID> {
    Optional<RefreshSession> findByToken(String token);
    void deleteByUser(User user);
}