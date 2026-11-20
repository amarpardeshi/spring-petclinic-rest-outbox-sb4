package com.capgemini.assessment.petclinic.domain.repository;

import com.capgemini.assessment.petclinic.domain.model.Pet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Pet aggregate persistence operations.
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    /**
     * Retrieves all pets belonging to a given owner ID.
     *
     * @param ownerId unique identifier of the owner
     * @return list of Pet entities owned by the specified owner
     */
    List<Pet> findByOwnerId(Long ownerId);
}
