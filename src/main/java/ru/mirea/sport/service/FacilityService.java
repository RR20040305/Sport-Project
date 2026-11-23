package ru.mirea.sport.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.sport.domain.Facility;
import ru.mirea.sport.dto.facility.FacilityRequest;
import ru.mirea.sport.dto.facility.FacilityResponse;
import ru.mirea.sport.repository.FacilityRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FacilityService {
    private final FacilityRepository repository;

    public FacilityService(FacilityRepository repository) {
        this.repository = repository;
    }

    public List<FacilityResponse> getAll() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public FacilityResponse create(FacilityRequest request) {
        if (repository.findByName(request.name()).isPresent()) {
            throw new RuntimeException("Площадка с таким названием уже существует");
        }
        Facility facility = new Facility();
        facility.setId(UUID.randomUUID());
        facility.setName(request.name());
        facility.setDescription(request.description());
        facility.setActive(request.active() != null && request.active());
        facility.setPricePerHour(request.pricePerHour());
        return mapToDto(repository.save(facility));
    }

    @Transactional
    public FacilityResponse update(UUID id, FacilityRequest request) {
        Facility facility = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Площадка не найдена"));
        facility.setName(request.name());
        facility.setDescription(request.description());
        if (request.active() != null) {
            facility.setActive(request.active());
        }
        facility.setPricePerHour(request.pricePerHour());
        return mapToDto(repository.save(facility));
    }

    @Transactional
    public void toggleActive(UUID id) {
        Facility facility = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Площадка не найдена"));
        facility.setActive(!facility.isActive());
        repository.save(facility);
    }

    private FacilityResponse mapToDto(Facility facility) {
        return new FacilityResponse(
            facility.getId(), 
            facility.getName(), 
            facility.getDescription(), 
            facility.isActive(),
            facility.getPricePerHour()
        );
    }
}