package com.capgemini.assessment.petclinic.integration.model;

import com.capgemini.assessment.petclinic.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Consumer-side inbox deduplication record ensuring idempotent processing of redelivered webhook events.
 */
@Entity
@Table(name = "processed_webhook_events")
public class ProcessedWebhookEvent extends BaseEntity {

    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 48)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    @Lob
    @Column(name = "payload_json", nullable = false)
    private String payloadJson;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    /**
     * Default constructor required by JPA.
     */
    public ProcessedWebhookEvent() {
    }

    /**
     * Constructs a ProcessedWebhookEvent record for inbox deduplication.
     *
     * @param eventId unique event UUID from X-Idempotency-Key or envelope
     * @param eventType event type string
     * @param aggregateId source aggregate ID
     * @param payloadHash SHA-256 payload checksum
     * @param payloadJson raw payload JSON
     * @param receivedAt UTC timestamp of first receipt
     */
    public ProcessedWebhookEvent(
            String eventId,
            String eventType,
            String aggregateId,
            String payloadHash,
            String payloadJson,
            Instant receivedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payloadHash = payloadHash;
        this.payloadJson = payloadJson;
        this.receivedAt = receivedAt;
    }

    /**
     * Returns the deduplicated event UUID.
     *
     * @return event UUID string
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the deduplicated event UUID.
     *
     * @param eventId event UUID string
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Returns the event type name.
     *
     * @return event type string
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Sets the event type name.
     *
     * @param eventType event type string
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the source aggregate identifier.
     *
     * @return aggregate ID
     */
    public String getAggregateId() {
        return aggregateId;
    }

    /**
     * Sets the source aggregate identifier.
     *
     * @param aggregateId aggregate ID
     */
    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    /**
     * Returns the SHA-256 checksum of the received payload.
     *
     * @return SHA-256 hash string
     */
    public String getPayloadHash() {
        return payloadHash;
    }

    /**
     * Sets the SHA-256 checksum of the received payload.
     *
     * @param payloadHash SHA-256 hash string
     */
    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    /**
     * Returns the raw JSON payload received.
     *
     * @return JSON string
     */
    public String getPayloadJson() {
        return payloadJson;
    }

    /**
     * Sets the raw JSON payload received.
     *
     * @param payloadJson JSON string
     */
    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    /**
     * Returns the UTC timestamp when the event was first processed.
     *
     * @return receivedAt timestamp
     */
    public Instant getReceivedAt() {
        return receivedAt;
    }

    /**
     * Sets the UTC timestamp when the event was first processed.
     *
     * @param receivedAt receivedAt timestamp
     */
    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}
