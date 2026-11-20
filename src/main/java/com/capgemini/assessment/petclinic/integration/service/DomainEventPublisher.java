package com.capgemini.assessment.petclinic.integration.service;

import com.capgemini.assessment.petclinic.integration.model.AggregateType;
import com.capgemini.assessment.petclinic.integration.model.DomainEventType;

/**
 * Port interface for recording PetClinic domain state mutations into the reliable integration layer.
 */
public interface DomainEventPublisher {

    /**
     * Records a domain mutation event inside the caller's active transaction boundary.
     *
     * @param aggregateType type of PetClinic aggregate root (OWNER, PET, VISIT)
     * @param aggregateId unique identifier of the mutated aggregate
     * @param eventType specific lifecycle mutation type
     * @param payloadSnapshot DTO or domain snapshot to serialize as JSON
     */
    void publishDomainEvent(
            AggregateType aggregateType,
            String aggregateId,
            DomainEventType eventType,
            Object payloadSnapshot
    );
}
