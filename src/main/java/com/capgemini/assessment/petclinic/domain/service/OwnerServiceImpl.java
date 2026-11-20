package com.capgemini.assessment.petclinic.domain.service;

import com.capgemini.assessment.petclinic.common.exception.ResourceNotFoundException;
import com.capgemini.assessment.petclinic.domain.dto.OwnerRequestDto;
import com.capgemini.assessment.petclinic.domain.dto.OwnerResponseDto;
import com.capgemini.assessment.petclinic.domain.model.Owner;
import com.capgemini.assessment.petclinic.domain.repository.OwnerRepository;
import com.capgemini.assessment.petclinic.integration.model.AggregateType;
import com.capgemini.assessment.petclinic.integration.model.DomainEventType;
import com.capgemini.assessment.petclinic.integration.service.DomainEventPublisher;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional implementation of OwnerService that persists Owner changes and publishes Outbox events atomically.
 */
@Service
public class OwnerServiceImpl implements OwnerService {

    private final OwnerRepository ownerRepository;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Constructs OwnerServiceImpl with repository and domain event publisher dependencies.
     *
     * @param ownerRepository JPA repository for Owner entities
     * @param domainEventPublisher outbox event publisher port
     */
    public OwnerServiceImpl(
            OwnerRepository ownerRepository,
            DomainEventPublisher domainEventPublisher) {
        this.ownerRepository = ownerRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * Retrieves all owners or filters by lastName prefix when provided.
     *
     * @param lastName optional last name prefix
     * @return list of OwnerResponseDto objects
     */
    @Override
    @Transactional(readOnly = true)
    public List<OwnerResponseDto> findOwners(String lastName) {
        List<Owner> owners = (lastName == null || lastName.isBlank())
                ? ownerRepository.findAll()
                : ownerRepository.findByLastNameStartingWithIgnoreCase(lastName.trim());
        return owners.stream().map(this::toDto).toList();
    }

    /**
     * Retrieves an Owner by ID or throws ResourceNotFoundException.
     *
     * @param ownerId unique owner identifier
     * @return OwnerResponseDto
     */
    @Override
    @Transactional(readOnly = true)
    public OwnerResponseDto findOwnerById(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner", ownerId));
        return toDto(owner);
    }

    /**
     * Persists a new Owner and records an OWNER_CREATED event in the transactional outbox.
     *
     * @param request validated owner creation payload
     * @return newly created OwnerResponseDto
     */
    @Override
    @Transactional
    public OwnerResponseDto createOwner(OwnerRequestDto request) {
        Owner owner = new Owner(
                request.firstName(),
                request.lastName(),
                request.address(),
                request.city(),
                request.telephone()
        );
        Owner saved = ownerRepository.save(owner);
        OwnerResponseDto dto = toDto(saved);
        domainEventPublisher.publishDomainEvent(
                AggregateType.OWNER,
                String.valueOf(saved.getId()),
                DomainEventType.OWNER_CREATED,
                dto
        );
        return dto;
    }

    /**
     * Updates an existing Owner and records an OWNER_UPDATED event in the transactional outbox.
     *
     * @param ownerId unique owner identifier
     * @param request validated owner update payload
     * @return updated OwnerResponseDto
     */
    @Override
    @Transactional
    public OwnerResponseDto updateOwner(Long ownerId, OwnerRequestDto request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner", ownerId));
        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        owner.setAddress(request.address());
        owner.setCity(request.city());
        owner.setTelephone(request.telephone());
        Owner saved = ownerRepository.save(owner);
        OwnerResponseDto dto = toDto(saved);
        domainEventPublisher.publishDomainEvent(
                AggregateType.OWNER,
                String.valueOf(saved.getId()),
                DomainEventType.OWNER_UPDATED,
                dto
        );
        return dto;
    }

    /**
     * Deletes an existing Owner and records an OWNER_DELETED event in the transactional outbox.
     *
     * @param ownerId unique owner identifier
     */
    @Override
    @Transactional
    public void deleteOwner(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner", ownerId));
        OwnerResponseDto snapshot = toDto(owner);
        ownerRepository.delete(owner);
        domainEventPublisher.publishDomainEvent(
                AggregateType.OWNER,
                String.valueOf(ownerId),
                DomainEventType.OWNER_DELETED,
                snapshot
        );
    }

    /**
     * Maps an Owner JPA entity to an immutable OwnerResponseDto.
     *
     * @param owner Owner entity
     * @return OwnerResponseDto instance
     */
    private OwnerResponseDto toDto(Owner owner) {
        return new OwnerResponseDto(
                owner.getId(),
                owner.getFirstName(),
                owner.getLastName(),
                owner.getAddress(),
                owner.getCity(),
                owner.getTelephone(),
                owner.getCreatedAt(),
                owner.getUpdatedAt()
        );
    }
}
