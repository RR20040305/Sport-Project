package ru.mirea.sport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.sport.domain.Facility;
import java.util.Optional;
import java.util.UUID;

public interface FacilityRepository extends JpaRepository<Facility, UUID> {
    Optional<Facility> findByName(String name);
}