package ru.mirea.sport.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mirea.sport.domain.Booking;
import ru.mirea.sport.dto.analytics.AnalyticsResponse;
import ru.mirea.sport.dto.analytics.GroupCountDto;
import ru.mirea.sport.repository.BookingRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private final BookingRepository bookingRepository;

    public AnalyticsService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public AnalyticsResponse getSummary() {
        List<Booking> all = bookingRepository.findAll();

        long total = all.size();
        long archived = all.stream().filter(Booking::isArchived).count();
        long active = total - archived;
        long paid = all.stream().filter(b -> b.isPaid() && !b.isArchived()).count();

        BigDecimal averagePrice = all.stream()
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(total > 0 ? total : 1), 2, RoundingMode.HALF_UP);

        List<GroupCountDto> byFacility = all.stream()
                .collect(Collectors.groupingBy(b -> b.getFacility().getName(), Collectors.counting()))
                .entrySet().stream().map(e -> new GroupCountDto(e.getKey(), e.getValue())).toList();

        List<GroupCountDto> byStatus = all.stream()
                .collect(Collectors.groupingBy(b -> b.getStatus().name(), Collectors.counting()))
                .entrySet().stream().map(e -> new GroupCountDto(e.getKey(), e.getValue())).toList();

        List<GroupCountDto> byPriority = all.stream()
                .collect(Collectors.groupingBy(b -> b.getPriorityLevel().name(), Collectors.counting()))
                .entrySet().stream().map(e -> new GroupCountDto(e.getKey(), e.getValue())).toList();

        return new AnalyticsResponse(total, active, archived, paid, averagePrice, byFacility, byStatus, byPriority);
    }
}