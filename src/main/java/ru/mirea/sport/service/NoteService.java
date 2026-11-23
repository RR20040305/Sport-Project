package ru.mirea.sport.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.sport.domain.Booking;
import ru.mirea.sport.domain.Note;
import ru.mirea.sport.domain.User;
import ru.mirea.sport.dto.note.NoteRequest;
import ru.mirea.sport.dto.note.NoteResponse;
import ru.mirea.sport.repository.BookingRepository;
import ru.mirea.sport.repository.NoteRepository;
import ru.mirea.sport.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, BookingRepository bookingRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    public List<NoteResponse> getNotesForBooking(UUID bookingId) {
        return noteRepository.findByBookingIdOrderByPinnedDescCreatedAtDesc(bookingId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public NoteResponse create(UUID bookingId, UUID authorId, NoteRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование не найдено"));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Note note = new Note();
        note.setId(UUID.randomUUID());
        note.setBooking(booking);
        note.setAuthor(author);
        note.setContent(request.content());
        note.setPinned(request.pinned());
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        return mapToDto(noteRepository.save(note));
    }

    @Transactional
    public NoteResponse update(UUID noteId, NoteRequest request) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Заметка не найдена"));
        note.setContent(request.content());
        note.setPinned(request.pinned());
        note.setUpdatedAt(LocalDateTime.now());
        return mapToDto(noteRepository.save(note));
    }

    @Transactional
    public void delete(UUID noteId) {
        noteRepository.deleteById(noteId);
    }

    private NoteResponse mapToDto(Note note) {
        return new NoteResponse(
            note.getId(),
            note.getContent(),
            note.isPinned(),
            note.getAuthor().getFullName(),
            note.getCreatedAt(),
            note.getUpdatedAt()
        );
    }
}