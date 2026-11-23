package ru.mirea.sport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.mirea.sport.domain.Booking;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {
    Optional<Booking> findByBookingCode(String bookingCode);
    boolean existsByBookingCode(String bookingCode);
}