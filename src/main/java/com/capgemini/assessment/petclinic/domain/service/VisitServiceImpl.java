package com.capgemini.assessment.petclinic.domain.service;

import com.capgemini.assessment.petclinic.common.exception.ResourceNotFoundException;
import com.capgemini.assessment.petclinic.domain.dto.VisitRequestDto;
import com.capgemini.assessment.petclinic.domain.dto.VisitResponseDto;
import com.capgemini.assessment.petclinic.domain.model.Pet;
import com.capgemini.assessment.petclinic.domain.model.Visit;
import com.capgemini.assessment.petclinic.domain.repository.PetRepository;
import com.capgemini.assessment.petclinic.domain.repository.VisitRepository;
import com.capgemini.assessment.petclinic.integration.model.AggregateType;
import com.capgemini.assessment.petclinic.integration.model.DomainEventType;
import com.capgemini.assessment.petclinic.integration.service.DomainEventPublisher;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional implementation of VisitService that persists Visit changes and publishes Outbox events atomically.
 */
@Service
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;
    private final PetRepository petRepository;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Constructs VisitServiceImpl with repositories and domain event publisher dependencies.
     *
     * @param visitRepository JPA repository for Visit entities
     * @param petRepository JPA repository for Pet entities
     * @param domainEventPublisher outbox event publisher port
     */
    public VisitServiceImpl(
            VisitRepository visitRepository,
            PetRepository petRepository,
            DomainEventPublisher domainEventPublisher) {
        this.visitRepository = visitRepository;
        this.petRepository = petRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * Retrieves all visits or filters by petId when provided.
     *
     * @param petId optional pet ID filter
     * @return list of VisitResponseDto objects
     */
    @Override
    @Transactional(readOnly = true)
    public List<VisitResponseDto> findVisits(Long petId) {
        List<Visit> visits = (petId == null)
                ? visitRepository.findAll()
                : visitRepository.findByPetId(petId);
        return visits.stream().map(this::toDto).toList();
    }

    /**
     * Retrieves a Visit by ID or throws ResourceNotFoundException.
     *
     * @param visitId unique visit identifier
     * @return VisitResponseDto
     */
    @Override
    @Transactional(readOnly = true)
    public VisitResponseDto findVisitById(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit", visitId));
        return toDto(visit);
    }

    /**
     * Schedules a new Visit for an existing Pet and records a VISIT_CREATED outbox event.
     *
     * @param request validated visit creation payload
     * @return created VisitResponseDto
     */
    @Override
    @Transactional
    public VisitResponseDto createVisit(VisitRequestDto request) {
        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", request.petId()));
        Visit visit = new Visit(request.visitDate(), request.description(), request.vetName(), pet);
        Visit saved = visitRepository.save(visit);
        VisitResponseDto dto = toDto(saved);
        domainEventPublisher.publishDomainEvent(
                AggregateType.VISIT,
                String.valueOf(saved.getId()),
                DomainEventType.VISIT_CREATED,
                dto
        );
        return dto;
    }

    /**
     * Updates an existing Visit and records a VISIT_UPDATED outbox event.
     *
     * @param visitId unique visit identifier
     * @param request validated visit update payload
     * @return updated VisitResponseDto
     */
    @Override
    @Transactional
    public VisitResponseDto updateVisit(Long visitId, VisitRequestDto request) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit", visitId));
        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", request.petId()));
        visit.setVisitDate(request.visitDate());
        visit.setDescription(request.description());
        visit.setVetName(request.vetName());
        visit.setPet(pet);
        Visit saved = visitRepository.save(visit);
        VisitResponseDto dto = toDto(saved);
        domainEventPublisher.publishDomainEvent(
                AggregateType.VISIT,
                String.valueOf(saved.getId()),
                DomainEventType.VISIT_UPDATED,
                dto
        );
        return dto;
    }

    /**
     * Deletes a Visit by ID and records a VISIT_DELETED outbox event.
     *
     * @param visitId unique visit identifier
     */
    @Override
    @Transactional
    public void deleteVisit(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit", visitId));
        VisitResponseDto snapshot = toDto(visit);
        visitRepository.delete(visit);
        domainEventPublisher.publishDomainEvent(
                AggregateType.VISIT,
                String.valueOf(visitId),
                DomainEventType.VISIT_DELETED,
                snapshot
        );
    }

    /**
     * Maps a Visit JPA entity to an immutable VisitResponseDto.
     *
     * @param visit Visit entity
     * @return VisitResponseDto instance
     */
    private VisitResponseDto toDto(Visit visit) {
        Pet pet = visit.getPet();
        return new VisitResponseDto(
                visit.getId(),
                visit.getVisitDate(),
                visit.getDescription(),
                visit.getVetName(),
                pet.getId(),
                pet.getName(),
                visit.getCreatedAt(),
                visit.getUpdatedAt()
        );
    }
}
