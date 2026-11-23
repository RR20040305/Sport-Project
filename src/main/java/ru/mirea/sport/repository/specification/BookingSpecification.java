package ru.mirea.sport.repository.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.mirea.sport.domain.Booking;
import ru.mirea.sport.domain.Tag;
import ru.mirea.sport.dto.booking.BookingFilter;

import java.util.ArrayList;
import java.util.List;

public class BookingSpecification {

    public static Specification<Booking> build(BookingFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Поиск по тексту
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String likePattern = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("clientName")), likePattern),
                        cb.like(cb.lower(root.get("bookingCode")), likePattern),
                        cb.like(cb.lower(root.get("clientPhone")), likePattern)
                ));
            }

            // 2. Фильтр по справочнику (Facility)
            if (filter.getFacilityId() != null) {
                predicates.add(cb.equal(root.get("facility").get("id"), filter.getFacilityId()));
            }

            // 3. Фильтр по статусу
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // 4. Фильтр по логическому признаку
            if (filter.getIsPaid() != null) {
                predicates.add(cb.equal(root.get("isPaid"), filter.getIsPaid()));
            }

            // 5. Фильтр по вычисляемому показателю (Приоритет)
            if (filter.getPriority() != null) {
                predicates.add(cb.equal(root.get("priorityLevel"), filter.getPriority()));
            }

            // 6. Фильтр по архивным / активным
            if (filter.getArchived() != null) {
                predicates.add(cb.equal(root.get("archived"), filter.getArchived()));
            } else {
                predicates.add(cb.isFalse(root.get("archived"))); // По умолчанию показываем только активные
            }

            // 7. Фильтр по числовому показателю с операцией сравнения
            if (filter.getPriceValue() != null && filter.getPriceOp() != null) {
                switch (filter.getPriceOp()) {
                    case GREATER_THAN -> predicates.add(cb.greaterThan(root.get("totalPrice"), filter.getPriceValue()));
                    case LESS_THAN -> predicates.add(cb.lessThan(root.get("totalPrice"), filter.getPriceValue()));
                    case EQUAL -> predicates.add(cb.equal(root.get("totalPrice"), filter.getPriceValue()));
                }
            }

            // 8. Фильтр по тегу
            if (filter.getTagName() != null && !filter.getTagName().isBlank()) {
                Join<Booking, Tag> tagsJoin = root.join("tags", JoinType.INNER);
                predicates.add(cb.equal(tagsJoin.get("name"), filter.getTagName()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}