package com.capgemini.assessment.petclinic.integration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * CloudEvents 1.0 inspired wire envelope transmitted to external systems and returned via the pull feed.
 *
 * @param specVersion wire specification version
 * @param eventId globally unique UUID for idempotency
 * @param sequenceNumber monotonically increasing outbox primary key for ordering
 * @param eventType domain mutation type
 * @param aggregateType aggregate root category
 * @param aggregateId domain entity ID
 * @param occurredAt UTC timestamp when the mutation was committed
 * @param payloadHash SHA-256 hex digest of the serialized domain snapshot
 * @param attemptCount delivery attempt number (1-based on outbound transmission)
 * @param data serialized JSON representation of the domain state
 */
@Schema(description = "CloudEvents-aligned integration event envelope")
public record IntegrationEventEnvelope(
        @Schema(description = "Specification version", example = "1.0")
        String specVersion,
        @Schema(description = "Unique event UUID (Idempotency Key)", example = "6b2f8d42-9c11-4f88-8b2a-1c3d5e7f9a01")
        String eventId,
        @Schema(description = "Monotonic database sequence number", example = "1")
        Long sequenceNumber,
        @Schema(description = "Domain mutation event type", example = "OWNER_CREATED")
        String eventType,
        @Schema(description = "Aggregate root type", example = "OWNER")
        String aggregateType,
        @Schema(description = "Aggregate identifier", example = "1")
        String aggregateId,
        @Schema(description = "UTC timestamp when change occurred")
        Instant occurredAt,
        @Schema(description = "SHA-256 checksum of data payload")
        String payloadHash,
        @Schema(description = "Current delivery attempt number", example = "1")
        int attemptCount,
        @Schema(description = "Serialized JSON domain snapshot")
        String data
) {
}
