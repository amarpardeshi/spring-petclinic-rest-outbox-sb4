package com.capgemini.assessment.petclinic.integration.repository;

import com.capgemini.assessment.petclinic.integration.model.OutboxEvent;
import com.capgemini.assessment.petclinic.integration.model.OutboxStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for querying and updating persistent Transactional Outbox events.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Finds a batch of outbox events eligible for delivery whose status is in the given set and nextAttemptAt is due.
     *
     * @param statuses eligible statuses (typically PENDING and FAILED_RETRYABLE)
     * @param now current UTC timestamp
     * @param pageable page size limit and sort order
     * @return ordered list of due OutboxEvent records
     */
    @Query("SELECT e FROM OutboxEvent e WHERE e.status IN :statuses AND e.nextAttemptAt <= :now ORDER BY e.id ASC")
    List<OutboxEvent> findDueEvents(
            @Param("statuses") Collection<OutboxStatus> statuses,
            @Param("now") Instant now,
            Pageable pageable
    );

    /**
     * Finds an outbox event by its unique UUID eventId.
     *
     * @param eventId unique UUID string
     * @return optional OutboxEvent
     */
    Optional<OutboxEvent> findByEventId(String eventId);

    /**
     * Finds all outbox events matching a specific status ordered by ID ascending.
     *
     * @param status target OutboxStatus
     * @return matching list of OutboxEvent records
     */
    List<OutboxEvent> findByStatusOrderByIdAsc(OutboxStatus status);

    /**
     * Retrieves cursor-paginated outbox events with sequence ID strictly greater than afterSequenceId for pull catch-up.
     *
     * @param afterSequenceId last seen monotonic sequence ID
     * @param pageable page size limit
     * @return ordered list of OutboxEvent records for the pull feed
     */
    List<OutboxEvent> findByIdGreaterThanOrderByIdAsc(Long afterSequenceId, Pageable pageable);

    /**
     * Counts the number of outbox events currently in the specified status.
     *
     * @param status target OutboxStatus
     * @return total count of matching records
     */
    long countByStatus(OutboxStatus status);
}
