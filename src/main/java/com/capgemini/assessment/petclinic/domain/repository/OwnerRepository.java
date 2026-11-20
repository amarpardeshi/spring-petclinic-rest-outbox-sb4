package com.capgemini.assessment.petclinic.domain.repository;

import com.capgemini.assessment.petclinic.domain.model.Owner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Owner aggregate persistence operations.
 */
@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    /**
     * Finds owners whose last name starts with the specified prefix (case-insensitive).
     *
     * @param lastNamePrefix prefix of the owner's last name
     * @return matching list of Owner entities
     */
    List<Owner> findByLastNameStartingWithIgnoreCase(String lastNamePrefix);
}
