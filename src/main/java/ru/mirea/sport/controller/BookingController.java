package ru.mirea.sport.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mirea.sport.dto.booking.BookingRequest;
import ru.mirea.sport.dto.booking.BookingResponse;
import ru.mirea.sport.service.AuditService;
import ru.mirea.sport.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import ru.mirea.sport.dto.booking.BookingFilter;
import ru.mirea.sport.dto.bulk.BulkRequest;
import ru.mirea.sport.dto.bulk.BulkResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import ru.mirea.sport.service.csv.CsvExportService;

import org.springframework.web.multipart.MultipartFile;
import ru.mirea.sport.dto.csv.CsvPreviewResponse;
import ru.mirea.sport.service.csv.CsvImportService;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final CsvExportService csvExportService;
    private final CsvImportService csvImportService;
    private final AuditService auditService;

    public BookingController(BookingService bookingService, CsvExportService csvExportService, CsvImportService csvImportService, AuditService auditService) {
        this.bookingService = bookingService;
        this.csvExportService = csvExportService;
        this.csvImportService = csvImportService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getAll(
            BookingFilter filter,
            @PageableDefault(size = 20) Pageable pageable) { 
        return ResponseEntity.ok(bookingService.getAll(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request, HttpServletRequest httpRequest) {
        BookingResponse response = bookingService.create(request);
        String correlationId = (String) httpRequest.getAttribute("correlationId");
        auditService.log("CREATE", "Booking", response.id(), "Создано бронирование " + response.bookingCode(), correlationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> update(@PathVariable UUID id, @Valid @RequestBody BookingRequest request, HttpServletRequest httpRequest) {
        BookingResponse response = bookingService.update(id, request);
        String correlationId = (String) httpRequest.getAttribute("correlationId");
        auditService.log("UPDATE", "Booking", id, "Обновлено бронирование " + response.bookingCode(), correlationId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        bookingService.archive(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        bookingService.restore(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')") // Только администратор (как в задании)
    public ResponseEntity<Void> permanentDelete(@PathVariable UUID id) {
        bookingService.permanentDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkResult> bulkAction(@Valid @RequestBody BulkRequest request) {
        return ResponseEntity.ok(bookingService.processBulk(request));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(BookingFilter filter) throws Exception {
        byte[] csv = csvExportService.exportBookings(filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bookings.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping("/import/preview")
    public ResponseEntity<CsvPreviewResponse> previewImport(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(csvImportService.preview(file));
    }

    @PostMapping("/import/apply")
    public ResponseEntity<Void> applyImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "ONLY_NEW") String mode) {
        csvImportService.apply(file, "ONLY_NEW".equalsIgnoreCase(mode));
        return ResponseEntity.ok().build();
    }
}