package ru.mirea.sport.service.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mirea.sport.domain.*;
import ru.mirea.sport.dto.csv.CsvPreviewResponse;
import ru.mirea.sport.repository.BookingRepository;
import ru.mirea.sport.repository.FacilityRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CsvImportService {
    private final BookingRepository bookingRepository;
    private final FacilityRepository facilityRepository;

    public CsvImportService(BookingRepository bookingRepository, FacilityRepository facilityRepository) {
        this.bookingRepository = bookingRepository;
        this.facilityRepository = facilityRepository;
    }

    public CsvPreviewResponse preview(MultipartFile file) {
        int valid = 0;
        List<String> errors = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            // Пропускаем BOM если есть
            reader.mark(1);
            if (reader.read() != '\uFEFF') reader.reset();

            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            int lineNum = 1;
            for (CSVRecord record : parser) {
                lineNum++;
                try {
                    validateRecord(record);
                    valid++;
                } catch (Exception e) {
                    errors.add("Строка " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Ошибка чтения файла: " + e.getMessage());
        }
        return new CsvPreviewResponse(valid, errors.size(), errors);
    }

    @Transactional
    public void apply(MultipartFile file, boolean onlyNew) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.mark(1);
            if (reader.read() != '\uFEFF') reader.reset();

            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            for (CSVRecord record : parser) {
                try {
                    String code = record.get("Код");
                    Optional<Booking> existing = bookingRepository.findByBookingCode(code);

                    if (existing.isPresent() && onlyNew) {
                        continue; // Пропускаем, если режим "только новые"
                    }

                    Booking booking = existing.orElseGet(Booking::new);
                    if (booking.getId() == null) {
                        booking.setId(UUID.randomUUID());
                        booking.setCreatedAt(LocalDateTime.now());
                    }

                    String facilityName = record.get("Площадка");
                    Facility facility = facilityRepository.findByName(facilityName)
                            .orElseThrow(() -> new RuntimeException("Площадка не найдена: " + facilityName));

                    booking.setBookingCode(code);
                    booking.setFacility(facility);
                    booking.setClientName(record.get("Клиент"));
                    booking.setClientPhone(record.get("Телефон"));
                    booking.setStartTime(LocalDateTime.parse(record.get("Начало")));
                    booking.setEndTime(LocalDateTime.parse(record.get("Конец")));
                    booking.setStatus(BookingStatus.valueOf(record.get("Статус")));
                    booking.setTotalPrice(new BigDecimal(record.get("Цена")));
                    booking.setPaid(Boolean.parseBoolean(record.get("Оплачено")));
                    booking.setPriorityLevel(PriorityLevel.valueOf(record.get("Приоритет")));
                    booking.setArchived(Boolean.parseBoolean(record.get("Архив")));
                    booking.setUpdatedAt(LocalDateTime.now());

                    bookingRepository.save(booking);
                } catch (Exception ignored) {
                    // В методе apply мы пропускаем ошибочные строки, так как предпросмотр уже был
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка импорта: " + e.getMessage());
        }
    }

    private void validateRecord(CSVRecord record) {
        if (!record.isMapped("Код") || record.get("Код").isBlank()) throw new RuntimeException("Пустой Код");
        if (!record.isMapped("Клиент") || record.get("Клиент").isBlank()) throw new RuntimeException("Пустой Клиент");
        try { LocalDateTime.parse(record.get("Начало")); } catch (Exception e) { throw new RuntimeException("Неверная дата Начало"); }
        try { new BigDecimal(record.get("Цена")); } catch (Exception e) { throw new RuntimeException("Неверная Цена"); }
        if (facilityRepository.findByName(record.get("Площадка")).isEmpty()) throw new RuntimeException("Площадка не существует");
    }
}