package ru.mirea.sport.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.sport.domain.Booking;
import ru.mirea.sport.domain.Facility;
import ru.mirea.sport.dto.booking.BookingRequest;
import ru.mirea.sport.dto.booking.BookingResponse;
import ru.mirea.sport.repository.BookingRepository;
import ru.mirea.sport.repository.FacilityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.mirea.sport.dto.booking.BookingFilter;
import ru.mirea.sport.repository.specification.BookingSpecification;
import ru.mirea.sport.dto.bulk.BulkAction;
import ru.mirea.sport.dto.bulk.BulkRequest;
import ru.mirea.sport.dto.bulk.BulkResult;
import ru.mirea.sport.domain.Tag;
import ru.mirea.sport.repository.TagRepository;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final FacilityRepository facilityRepository;
    private final TagRepository tagRepository;

    public BookingService(BookingRepository bookingRepository, FacilityRepository facilityRepository, TagRepository tagRepository) {
        this.bookingRepository = bookingRepository;
        this.facilityRepository = facilityRepository;
        this.tagRepository = tagRepository;
    }

    public Page<BookingResponse> getAll(BookingFilter filter, Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new RuntimeException("Максимальный размер страницы - 100 элементов");
        }
        return bookingRepository.findAll(BookingSpecification.build(filter), pageable)
                .map(this::mapToDto);
    }

    public BookingResponse getById(UUID id) {
        return bookingRepository.findById(id).map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование не найдено"));
    }

    @Transactional
    public BookingResponse create(BookingRequest request) {
        Facility facility = facilityRepository.findById(request.facilityId())
                .orElseThrow(() -> new EntityNotFoundException("Площадка не найдена"));

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setBookingCode("BR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        
        applyRequest(booking, request, facility);
        return mapToDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse update(UUID id, BookingRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование не найдено"));

        if (!booking.getVersion().equals(request.version())) {
            throw new org.springframework.dao.OptimisticLockingFailureException("Конфликт версий");
        }

        Facility facility = facilityRepository.findById(request.facilityId())
                .orElseThrow(() -> new EntityNotFoundException("Площадка не найдена"));

        applyRequest(booking, request, facility);
        return mapToDto(bookingRepository.save(booking));
    }

    @Transactional
    public void archive(UUID id) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setArchived(true);
        bookingRepository.save(booking);
    }

    @Transactional
    public void restore(UUID id) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setArchived(false);
        bookingRepository.save(booking);
    }

    @Transactional
    public void permanentDelete(UUID id) {
        bookingRepository.deleteById(id);
    }

    private void applyRequest(Booking booking, BookingRequest request, Facility facility) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new RuntimeException("Время окончания должно быть позже времени начала");
        }

        long minutes = Duration.between(request.startTime(), request.endTime()).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        
        booking.setFacility(facility);
        booking.setClientName(request.clientName());
        booking.setClientPhone(request.clientPhone());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setStatus(request.status());
        booking.setTotalPrice(facility.getPricePerHour().multiply(hours));
        booking.setPaid(request.isPaid());
        booking.setPriorityLevel(request.priorityLevel());
        booking.setUpdatedAt(LocalDateTime.now());
    }

    private BookingResponse mapToDto(Booking booking) {
        return new BookingResponse(
            booking.getId(), booking.getBookingCode(), booking.getFacility().getName(),
            booking.getClientName(), booking.getClientPhone(), booking.getStartTime(),
            booking.getEndTime(), booking.getStatus(), booking.getTotalPrice(),
            booking.isPaid(), booking.getPriorityLevel(), booking.getCreatedAt(),
            booking.getUpdatedAt(), booking.getVersion(), booking.isArchived()
        );
    }

    @Transactional
    public BulkResult processBulk(BulkRequest request) {
        int success = 0;
        List<String> errors = new ArrayList<>();

        for (UUID id : request.ids()) {
            try {
                Booking booking = bookingRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("ID " + id + " не найден"));

                switch (request.action()) {
                    case ARCHIVE -> booking.setArchived(true);
                    case RESTORE -> booking.setArchived(false);
                    case CHANGE_STATUS -> {
                        if (request.statusValue() == null) throw new RuntimeException("Укажите статус");
                        booking.setStatus(request.statusValue());
                    }
                    case ADD_TAG, REMOVE_TAG -> {
                        if (request.tagValue() == null || request.tagValue().isBlank()) 
                            throw new RuntimeException("Укажите тег");
                        Tag tag = tagRepository.findByName(request.tagValue())
                                .orElseGet(() -> {
                                    Tag newTag = new Tag();
                                    newTag.setId(UUID.randomUUID());
                                    newTag.setName(request.tagValue());
                                    return tagRepository.save(newTag);
                                });
                        
                        if (request.action() == BulkAction.ADD_TAG) booking.getTags().add(tag);
                        else booking.getTags().remove(tag);
                    }
                }
                booking.setUpdatedAt(LocalDateTime.now());
                bookingRepository.save(booking);
                success++;
            } catch (Exception e) {
                errors.add(e.getMessage());
            }
        }
        return new BulkResult(success, errors.size(), errors);
    }
}