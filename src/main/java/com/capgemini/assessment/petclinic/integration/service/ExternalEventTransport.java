package com.capgemini.assessment.petclinic.integration.service;

import com.capgemini.assessment.petclinic.integration.dto.IntegrationEventEnvelope;
import com.capgemini.assessment.petclinic.integration.dto.TransportDeliveryResult;

/**
 * Outbound transport abstraction (Dependency Inversion Principle) for delivering integration event envelopes.
 */
public interface ExternalEventTransport {

    /**
     * Transmits an IntegrationEventEnvelope to the configured external target system.
     *
     * @param envelope wire-level event envelope containing idempotency metadata and data snapshot
     * @return TransportDeliveryResult describing success or failure diagnostics
     */
    TransportDeliveryResult send(IntegrationEventEnvelope envelope);
}
