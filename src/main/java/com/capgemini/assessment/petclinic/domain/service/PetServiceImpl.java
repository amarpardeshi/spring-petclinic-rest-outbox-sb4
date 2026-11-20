package com.capgemini.assessment.petclinic.domain.service;

import com.capgemini.assessment.petclinic.common.exception.ResourceNotFoundException;
import com.capgemini.assessment.petclinic.domain.dto.PetRequestDto;
import com.capgemini.assessment.petclinic.domain.dto.PetResponseDto;
import com.capgemini.assessment.petclinic.domain.model.Owner;
import com.capgemini.assessment.petclinic.domain.model.Pet;
import com.capgemini.assessment.petclinic.domain.repository.OwnerRepository;
import com.capgemini.assessment.petclinic.domain.repository.PetRepository;
import com.capgemini.assessment.petclinic.integration.model.AggregateType;
import com.capgemini.assessment.petclinic.integration.model.DomainEventType;
import com.capgemini.assessment.petclinic.integration.service.DomainEventPublisher;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional implementation of PetService that persists Pet changes and publishes Outbox events atomically.
 */
@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Constructs PetServiceImpl with repositories and domain event publisher dependencies.
     *
     * @param petRepository JPA repository for Pet entities
     * @param ownerRepository JPA repository for Owner entities
     * @param domainEventPublisher outbox event publisher port
     */
    public PetServiceImpl(
            PetRepository petRepository,
            OwnerRepository ownerRepository,
            DomainEventPublisher domainEventPublisher) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * Retrieves all pets or filters by ownerId when provided.
     *
     * @param ownerId optional owner ID filter
     * @return list of PetResponseDto objects
     */
    @Override
    @Transactional(readOnly = true)
    public List<PetResponseDto> findPets(Long ownerId) {
        List<Pet> pets = (ownerId == null)
                ? petRepository.findAll()
                : petRepository.findByOwnerId(ownerId);
        return pets.stream().map(this::toDto).toList();
    }

    /**
     * Retrieves a Pet by ID or throws ResourceNotFoundException.
     *
     * @param petId unique pet identifier
     * @return PetResponseDto
     */
    @Override
    @Transactional(readOnly = true)
    public PetResponseDto findPetById(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));
        return toDto(pet);
    }

    /**
     * Creates a new Pet under an existing Owner and records a PET_CREATED outbox event.
     *
     * @param request validated pet creation payload
     * @return created PetResponseDto
     */
    @Override
    @Transactional
    public PetResponseDto createPet(PetRequestDto request) {
        Owner owner = ownerRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner", request.ownerId()));
        Pet pet = new Pet(request.name(), request.birthDate(), request.petType(), owner);
        Pet saved = petRepository.save(pet);
        PetResponseDto dto = toDto(saved);
        domainEventPublisher.publishDomainEvent(
                AggregateType.PET,
                String.valueOf(saved.getId()),
                DomainEventType.PET_CREATED,
                dto
        );
        return dto;
    }

    /**
     * Updates an existing Pet and records a PET_UPDATED outbox event.
     *
     * @param petId unique pet identifier
     * @param request validated pet update payload
     * @return updated PetResponseDto
     */
    @Override
    @Transactional
    public PetResponseDto updatePet(Long petId, PetRequestDto request) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));
        Owner owner = ownerRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner", request.ownerId()));
        pet.setName(request.name());
        pet.setBirthDate(request.birthDate());
        pet.setPetType(request.petType());
        pet.setOwner(owner);
        Pet saved = petRepository.save(pet);
        PetResponseDto dto = toDto(saved);
        domainEventPublisher.publishDomainEvent(
                AggregateType.PET,
                String.valueOf(saved.getId()),
                DomainEventType.PET_UPDATED,
                dto
        );
        return dto;
    }

    /**
     * Deletes a Pet by ID and records a PET_DELETED outbox event.
     *
     * @param petId unique pet identifier
     */
    @Override
    @Transactional
    public void deletePet(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));
        PetResponseDto snapshot = toDto(pet);
        petRepository.delete(pet);
        domainEventPublisher.publishDomainEvent(
                AggregateType.PET,
                String.valueOf(petId),
                DomainEventType.PET_DELETED,
                snapshot
        );
    }

    /**
     * Maps a Pet JPA entity to an immutable PetResponseDto.
     *
     * @param pet Pet entity
     * @return PetResponseDto instance
     */
    private PetResponseDto toDto(Pet pet) {
        Owner owner = pet.getOwner();
        String fullName = owner.getFirstName() + " " + owner.getLastName();
        return new PetResponseDto(
                pet.getId(),
                pet.getName(),
                pet.getBirthDate(),
                pet.getPetType(),
                owner.getId(),
                fullName,
                pet.getCreatedAt(),
                pet.getUpdatedAt()
        );
    }
}
