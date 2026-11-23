package ru.mirea.sport.service.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import ru.mirea.sport.domain.Booking;
import ru.mirea.sport.dto.booking.BookingFilter;
import ru.mirea.sport.repository.BookingRepository;
import ru.mirea.sport.repository.specification.BookingSpecification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CsvExportService {
    private final BookingRepository bookingRepository;

    public CsvExportService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public byte[] exportBookings(BookingFilter filter) throws IOException {
        List<Booking> bookings = bookingRepository.findAll(BookingSpecification.build(filter));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // BOM для корректного отображения кириллицы в Excel
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Код", "Площадка", "Клиент", "Телефон", "Начало", "Конец", "Статус", "Цена", "Оплачено", "Приоритет", "Архив"))) {

            for (Booking b : bookings) {
                csvPrinter.printRecord(
                        b.getBookingCode(),
                        b.getFacility().getName(),
                        b.getClientName(),
                        b.getClientPhone(),
                        b.getStartTime().toString(),
                        b.getEndTime().toString(),
                        b.getStatus().name(),
                        b.getTotalPrice().toString(),
                        b.isPaid(),
                        b.getPriorityLevel().name(),
                        b.isArchived()
                );
            }
        }
        return out.toByteArray();
    }
}