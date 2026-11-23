package ru.mirea.sport.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.sport.domain.AuditEvent;
import ru.mirea.sport.dto.audit.AuditEventResponse;
import ru.mirea.sport.repository.AuditEventRepository;
import ru.mirea.sport.repository.UserRepository;
import ru.mirea.sport.security.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository auditRepository;
    private final UserRepository userRepository;

    public AuditService(AuditEventRepository auditRepository, UserRepository userRepository) {
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String eventType, String entityType, UUID entityId, String description, String correlationId) {
        try {
            AuditEvent event = new AuditEvent();
            event.setId(UUID.randomUUID());
            event.setEventType(eventType);
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setDescription(description);
            event.setCorrelationId(correlationId);
            event.setCreatedAt(LocalDateTime.now());

            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                userRepository.findById(userDetails.getId()).ifPresent(event::setUser);
            }

            auditRepository.saveAndFlush(event);
            log.debug("Audit saved: {} - {}", eventType, description);
        } catch (Exception e) {
            log.error("Не удалось сохранить событие аудита: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Page<AuditEventResponse> getAuditLog(Pageable pageable) {
        return searchAuditLog(null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> searchAuditLog(String query, Pageable pageable) {
        Page<AuditEvent> events;
        if (query == null || query.isBlank()) {
            events = auditRepository.findAll(pageable);
        } else {
            events = auditRepository.findByEventTypeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable);
        }

        return events.map(event -> new AuditEventResponse(
                event.getId(),
                event.getUser() != null ? event.getUser().getEmail() : "SYSTEM",
                event.getEventType(),
                event.getEntityType(),
                event.getEntityId(),
                event.getCorrelationId(),
                event.getCreatedAt(),
                event.getDescription()
        ));
    }
}