package ru.mirea.sport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.sport.dto.booking.BookingRequest;
import ru.mirea.sport.domain.*;
import ru.mirea.sport.repository.FacilityRepository;
import ru.mirea.sport.service.BookingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingBusinessRuleTest {

    @Autowired private BookingService bookingService;
    @Autowired private FacilityRepository facilityRepository;

    @Test
    void shouldCalculateTotalPriceCorrectly() {
        // Создаем площадку с ценой 1000 руб/час
        Facility facility = new Facility();
        facility.setId(UUID.randomUUID());
        facility.setName("Зал 1");
        facility.setActive(true);
        facility.setPricePerHour(new BigDecimal("1000.00"));
        facilityRepository.save(facility);

        // Бронируем на 2.5 часа (150 минут)
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = LocalDateTime.now().plusDays(1).withHour(12).withMinute(30);

        BookingRequest request = new BookingRequest(
                facility.getId(), "Иван", "123", 
                start, end, 
                BookingStatus.CREATED, false, 
                PriorityLevel.LOW, null
        );

        var response = bookingService.create(request);
        
        // Цена должна быть 2.5 * 1000 = 2500
        assertThat(response.totalPrice()).isEqualByComparingTo(new BigDecimal("2500.00"));
        // Код должен сгенерироваться автоматически
        assertThat(response.bookingCode()).startsWith("BR-");
    }
}