package ru.mirea.sport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.sport.domain.Note;
import java.util.List;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByBookingIdOrderByPinnedDescCreatedAtDesc(UUID bookingId);
}