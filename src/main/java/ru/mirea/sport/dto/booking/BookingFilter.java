package ru.mirea.sport.dto.booking;

import ru.mirea.sport.domain.BookingStatus;
import ru.mirea.sport.domain.ComparisonOperation;
import ru.mirea.sport.domain.PriorityLevel;

import java.math.BigDecimal;
import java.util.UUID;

public class BookingFilter {
    private String search;                // Свободный текстовый поиск
    private UUID facilityId;              // По справочной сущности
    private BookingStatus status;         // По статусу
    private Boolean isPaid;               // По логическому признаку
    private PriorityLevel priority;       // По вычисляемому показателю
    private String tagName;               // По тегу
    private Boolean archived;             // По архивным/активным
    private BigDecimal priceValue;        // Числовой показатель
    private ComparisonOperation priceOp;  // Операция сравнения (>, <, =)

    // Геттеры и сеттеры (обязательно для Spring MVC)
    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }
    public UUID getFacilityId() { return facilityId; }
    public void setFacilityId(UUID facilityId) { this.facilityId = facilityId; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Boolean getIsPaid() { return isPaid; }
    public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }
    public PriorityLevel getPriority() { return priority; }
    public void setPriority(PriorityLevel priority) { this.priority = priority; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }
    public BigDecimal getPriceValue() { return priceValue; }
    public void setPriceValue(BigDecimal priceValue) { this.priceValue = priceValue; }
    public ComparisonOperation getPriceOp() { return priceOp; }
    public void setPriceOp(ComparisonOperation priceOp) { this.priceOp = priceOp; }
}