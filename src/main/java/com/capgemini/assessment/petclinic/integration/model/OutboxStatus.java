package com.capgemini.assessment.petclinic.integration.model;

/**
 * Lifecycle state machine states for a persistent Transactional Outbox event.
 */
public enum OutboxStatus {
    PENDING,
    FAILED_RETRYABLE,
    PUBLISHED,
    DEAD_LETTER
}
