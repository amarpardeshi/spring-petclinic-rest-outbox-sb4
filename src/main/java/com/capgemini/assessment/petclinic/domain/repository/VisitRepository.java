package com.capgemini.assessment.petclinic.domain.repository;

import com.capgemini.assessment.petclinic.domain.model.Visit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Visit aggregate persistence operations.
 */
@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    /**
     * Retrieves all veterinary visits scheduled for a specific pet ID.
     *
     * @param petId unique identifier of the pet
     * @return list of Visit entities for the specified pet
     */
    List<Visit> findByPetId(Long petId);
}
