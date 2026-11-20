package com.capgemini.assessment.petclinic.domain.service;

import com.capgemini.assessment.petclinic.domain.dto.PetRequestDto;
import com.capgemini.assessment.petclinic.domain.dto.PetResponseDto;
import java.util.List;

/**
 * Business service contract for managing Pet aggregates and recording pet lifecycle events.
 */
public interface PetService {

    /**
     * Retrieves all pets or filters by ownerId when provided.
     *
     * @param ownerId optional owner ID filter
     * @return list of PetResponseDto objects
     */
    List<PetResponseDto> findPets(Long ownerId);

    /**
     * Retrieves a single Pet by primary key ID.
     *
     * @param petId unique pet ID
     * @return PetResponseDto representation
     */
    PetResponseDto findPetById(Long petId);

    /**
     * Registers a new Pet and atomically records a PET_CREATED outbox event.
     *
     * @param request validated PetRequestDto
     * @return created PetResponseDto
     */
    PetResponseDto createPet(PetRequestDto request);

    /**
     * Updates an existing Pet and atomically records a PET_UPDATED outbox event.
     *
     * @param petId unique pet ID to update
     * @param request validated PetRequestDto
     * @return updated PetResponseDto
     */
    PetResponseDto updatePet(Long petId, PetRequestDto request);

    /**
     * Deletes a Pet by ID and atomically records a PET_DELETED outbox event.
     *
     * @param petId unique pet ID to delete
     */
    void deletePet(Long petId);
}
