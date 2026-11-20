package com.capgemini.assessment.petclinic.integration.repository;

import com.capgemini.assessment.petclinic.integration.model.ProcessedWebhookEvent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the consumer-side idempotent inbox table.
 */
@Repository
public interface ProcessedWebhookEventRepository extends JpaRepository<ProcessedWebhookEvent, Long> {

    /**
     * Looks up a previously processed webhook event by its unique idempotency eventId.
     *
     * @param eventId unique event UUID
     * @return optional ProcessedWebhookEvent if already handled
     */
    Optional<ProcessedWebhookEvent> findByEventId(String eventId);
}
