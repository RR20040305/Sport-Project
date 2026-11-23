package ru.mirea.sport.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.mirea.sport.domain.AuditEvent;

import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID>, JpaSpecificationExecutor<AuditEvent> {

    Page<AuditEvent> findByEventTypeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String eventType, String description, Pageable pageable);
}