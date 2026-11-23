package ru.mirea.sport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.sport.dto.booking.BookingFilter;
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
class BookingFilterTest {

    @Autowired private BookingService bookingService;
    @Autowired private FacilityRepository facilityRepository;

    @Test
    void shouldFindBookingByClientName() {
        Facility facility = new Facility();
        facility.setId(UUID.randomUUID());
        facility.setName("Зал 2");
        facility.setActive(true);
        facility.setPricePerHour(new BigDecimal("500"));
        facilityRepository.save(facility);

        // Убрали "CODE-1" и BigDecimal("500")
        bookingService.create(new BookingRequest(
                facility.getId(), "Александр", "111", 
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), 
                BookingStatus.CREATED, false, PriorityLevel.LOW, null
        ));
        
        // Убрали "CODE-2" и BigDecimal("600")
        bookingService.create(new BookingRequest(
                facility.getId(), "Дмитрий", "222", 
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), 
                BookingStatus.CREATED, false, PriorityLevel.LOW, null
        ));

        BookingFilter filter = new BookingFilter();
        filter.setSearch("Александр");

        var result = bookingService.getAll(filter, PageRequest.of(0, 10, Sort.by("bookingCode")));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).clientName()).isEqualTo("Александр");
    }
}