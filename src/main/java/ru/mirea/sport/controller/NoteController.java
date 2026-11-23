package ru.mirea.sport.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.mirea.sport.dto.note.NoteRequest;
import ru.mirea.sport.dto.note.NoteResponse;
import ru.mirea.sport.security.CustomUserDetails;
import ru.mirea.sport.service.NoteService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/bookings/{bookingId}/notes")
    public ResponseEntity<List<NoteResponse>> getNotes(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(noteService.getNotesForBooking(bookingId));
    }

    @PostMapping("/bookings/{bookingId}/notes")
    public ResponseEntity<NoteResponse> createNote(@PathVariable UUID bookingId,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @Valid @RequestBody NoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noteService.create(bookingId, userDetails.getId(), request));
    }

    @PutMapping("/notes/{noteId}")
    public ResponseEntity<NoteResponse> updateNote(@PathVariable UUID noteId,
                                                   @Valid @RequestBody NoteRequest request) {
        return ResponseEntity.ok(noteService.update(noteId, request));
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable UUID noteId) {
        noteService.delete(noteId);
        return ResponseEntity.noContent().build();
    }
}