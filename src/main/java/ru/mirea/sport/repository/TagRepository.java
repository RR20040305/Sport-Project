package ru.mirea.sport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.sport.domain.Tag;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByName(String name);
}