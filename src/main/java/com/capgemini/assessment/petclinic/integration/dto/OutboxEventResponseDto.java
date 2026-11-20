package com.capgemini.assessment.petclinic.integration.dto;

import com.capgemini.assessment.petclinic.integration.model.AggregateType;
import com.capgemini.assessment.petclinic.integration.model.DomainEventType;
import com.capgemini.assessment.petclinic.integration.model.OutboxStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Operational view of a persistent OutboxEvent record for observability, auditing, and replay inspection.
 *
 * @param id monotonic sequence ID
 * @param eventId unique event UUID
 * @param aggregateType aggregate root type
 * @param aggregateId aggregate primary key
 * @param eventType domain mutation type
 * @param status current delivery state
 * @param attemptCount number of delivery attempts made
 * @param nextAttemptAt next eligible retry timestamp
 * @param publishedAt timestamp of successful delivery
 * @param lastError diagnostic message from the latest failure
 * @param payloadHash SHA-256 checksum of payload
 * @param payloadJson serialized JSON snapshot
 * @param createdAt creation timestamp
 */
@Schema(description = "Outbox event record details for operational monitoring and recovery")
public record OutboxEventResponseDto(
        Long id,
        String eventId,
        AggregateType aggregateType,
        String aggregateId,
        DomainEventType eventType,
        OutboxStatus status,
        int attemptCount,
        Instant nextAttemptAt,
        Instant publishedAt,
        String lastError,
        String payloadHash,
        String payloadJson,
        Instant createdAt
) {
}
