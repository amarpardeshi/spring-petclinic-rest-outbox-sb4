package com.capgemini.assessment.petclinic.domain.service;

import com.capgemini.assessment.petclinic.domain.dto.OwnerRequestDto;
import com.capgemini.assessment.petclinic.domain.dto.OwnerResponseDto;
import java.util.List;

/**
 * Business service contract for managing PetClinic Owner aggregates and recording integration events.
 */
public interface OwnerService {

    /**
     * Retrieves all registered pet owners, optionally filtered by last name prefix.
     *
     * @param lastName optional last name prefix filter
     * @return list of OwnerResponseDto objects
     */
    List<OwnerResponseDto> findOwners(String lastName);

    /**
     * Retrieves a single Owner by primary key ID.
     *
     * @param ownerId unique owner ID
     * @return OwnerResponseDto representation
     */
    OwnerResponseDto findOwnerById(Long ownerId);

    /**
     * Creates a new Owner and atomically records an OWNER_CREATED outbox event.
     *
     * @param request validated OwnerRequestDto
     * @return created OwnerResponseDto
     */
    OwnerResponseDto createOwner(OwnerRequestDto request);

    /**
     * Updates an existing Owner and atomically records an OWNER_UPDATED outbox event.
     *
     * @param ownerId unique owner ID to update
     * @param request validated OwnerRequestDto
     * @return updated OwnerResponseDto
     */
    OwnerResponseDto updateOwner(Long ownerId, OwnerRequestDto request);

    /**
     * Deletes an Owner by ID and atomically records an OWNER_DELETED outbox event.
     *
     * @param ownerId unique owner ID to delete
     */
    void deleteOwner(Long ownerId);
}
