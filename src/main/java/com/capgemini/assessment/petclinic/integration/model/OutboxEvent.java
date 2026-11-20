package com.capgemini.assessment.petclinic.integration.model;

import com.capgemini.assessment.petclinic.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Persistent Transactional Outbox entity stored in the same local transaction as PetClinic domain changes.
 */
@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(name = "idx_outbox_status_next_attempt", columnList = "status, next_attempt_at"),
                @Index(name = "idx_outbox_event_id", columnList = "event_id", unique = true)
        }
)
public class OutboxEvent extends BaseEntity {

    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type", nullable = false, length = 32)
    private AggregateType aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 48)
    private DomainEventType eventType;

    @Lob
    @Column(name = "payload_json", nullable = false)
    private String payloadJson;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OutboxStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    /**
     * Default constructor required by JPA.
     */
    public OutboxEvent() {
    }

    /**
     * Constructs a newly recorded OutboxEvent in PENDING status.
     *
     * @param eventId globally unique idempotency UUID
     * @param aggregateType domain aggregate category
     * @param aggregateId domain aggregate primary key
     * @param eventType specific mutation event type
     * @param payloadJson serialized JSON snapshot of the aggregate state
     * @param payloadHash SHA-256 checksum of payloadJson
     * @param nextAttemptAt timestamp when the event is eligible for relay
     */
    public OutboxEvent(
            String eventId,
            AggregateType aggregateType,
            String aggregateId,
            DomainEventType eventType,
            String payloadJson,
            String payloadHash,
            Instant nextAttemptAt) {
        this.eventId = eventId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payloadJson = payloadJson;
        this.payloadHash = payloadHash;
        this.status = OutboxStatus.PENDING;
        this.attemptCount = 0;
        this.nextAttemptAt = nextAttemptAt;
    }

    /**
     * Returns the unique idempotency event UUID.
     *
     * @return event UUID string
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the unique idempotency event UUID.
     *
     * @param eventId event UUID string
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Returns the aggregate root type.
     *
     * @return AggregateType enum
     */
    public AggregateType getAggregateType() {
        return aggregateType;
    }

    /**
     * Sets the aggregate root type.
     *
     * @param aggregateType AggregateType enum
     */
    public void setAggregateType(AggregateType aggregateType) {
        this.aggregateType = aggregateType;
    }

    /**
     * Returns the domain aggregate identifier.
     *
     * @return aggregate ID string
     */
    public String getAggregateId() {
        return aggregateId;
    }

    /**
     * Sets the domain aggregate identifier.
     *
     * @param aggregateId aggregate ID string
     */
    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    /**
     * Returns the domain mutation event type.
     *
     * @return DomainEventType enum
     */
    public DomainEventType getEventType() {
        return eventType;
    }

    /**
     * Sets the domain mutation event type.
     *
     * @param eventType DomainEventType enum
     */
    public void setEventType(DomainEventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the serialized JSON payload snapshot.
     *
     * @return JSON string
     */
    public String getPayloadJson() {
        return payloadJson;
    }

    /**
     * Sets the serialized JSON payload snapshot.
     *
     * @param payloadJson JSON string
     */
    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    /**
     * Returns the SHA-256 hash of the payload.
     *
     * @return SHA-256 hex digest
     */
    public String getPayloadHash() {
        return payloadHash;
    }

    /**
     * Sets the SHA-256 hash of the payload.
     *
     * @param payloadHash SHA-256 hex digest
     */
    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    /**
     * Returns the current outbox delivery status.
     *
     * @return OutboxStatus enum
     */
    public OutboxStatus getStatus() {
        return status;
    }

    /**
     * Sets the current outbox delivery status.
     *
     * @param status OutboxStatus enum
     */
    public void setStatus(OutboxStatus status) {
        this.status = status;
    }

    /**
     * Returns the number of delivery attempts performed so far.
     *
     * @return delivery attempt counter
     */
    public int getAttemptCount() {
        return attemptCount;
    }

    /**
     * Sets the number of delivery attempts performed so far.
     *
     * @param attemptCount delivery attempt counter
     */
    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    /**
     * Returns the UTC timestamp for the next scheduled delivery attempt.
     *
     * @return next eligible attempt timestamp
     */
    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    /**
     * Sets the UTC timestamp for the next scheduled delivery attempt.
     *
     * @param nextAttemptAt next eligible attempt timestamp
     */
    public void setNextAttemptAt(Instant nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }

    /**
     * Returns the UTC timestamp when the event was acknowledged by the external system.
     *
     * @return published timestamp or null
     */
    public Instant getPublishedAt() {
        return publishedAt;
    }

    /**
     * Sets the UTC timestamp when the event was acknowledged by the external system.
     *
     * @param publishedAt published timestamp
     */
    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    /**
     * Returns the diagnostic message from the most recent failed delivery attempt.
     *
     * @return last error message or null
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Sets the diagnostic message from the most recent failed delivery attempt.
     *
     * @param lastError error diagnostic text
     */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}
