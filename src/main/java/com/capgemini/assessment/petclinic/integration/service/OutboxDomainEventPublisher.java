package com.capgemini.assessment.petclinic.integration.service;

import com.capgemini.assessment.petclinic.common.exception.IntegrationDeliveryException;
import com.capgemini.assessment.petclinic.common.util.HashUtils;
import com.capgemini.assessment.petclinic.integration.model.AggregateType;
import com.capgemini.assessment.petclinic.integration.model.DomainEventType;
import com.capgemini.assessment.petclinic.integration.model.OutboxEvent;
import com.capgemini.assessment.petclinic.integration.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional Outbox implementation of DomainEventPublisher that persists OutboxEvent rows in the active DB transaction.
 */
@Service
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxDomainEventPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructs OutboxDomainEventPublisher with required repository and JSON serializer dependencies.
     *
     * @param outboxEventRepository JPA repository for outbox events
     * @param objectMapper Jackson ObjectMapper for payload serialization
     */
    public OutboxDomainEventPublisher(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Serializes the domain snapshot, computes its SHA-256 hash, and persists a PENDING OutboxEvent record.
     *
     * @param aggregateType type of PetClinic aggregate root
     * @param aggregateId unique identifier of the aggregate
     * @param eventType specific mutation event type
     * @param payloadSnapshot domain snapshot object to serialize
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishDomainEvent(
            AggregateType aggregateType,
            String aggregateId,
            DomainEventType eventType,
            Object payloadSnapshot) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payloadSnapshot);
            String payloadHash = HashUtils.sha256Hex(payloadJson);
            String eventId = UUID.randomUUID().toString();
            OutboxEvent outboxEvent = new OutboxEvent(
                    eventId,
                    aggregateType,
                    aggregateId,
                    eventType,
                    payloadJson,
                    payloadHash,
                    Instant.now()
            );
            outboxEventRepository.save(outboxEvent);
            LOGGER.info("Recorded outbox event [eventId={}, type={}, aggregateId={}]",
                    eventId, eventType, aggregateId);
        } catch (JsonProcessingException exception) {
            throw new IntegrationDeliveryException("Failed to serialize domain event payload for outbox", exception);
        }
    }
}
