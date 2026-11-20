package com.capgemini.assessment.petclinic.integration.service;

import com.capgemini.assessment.petclinic.integration.dto.IntegrationEventEnvelope;
import com.capgemini.assessment.petclinic.integration.dto.OutboxEventResponseDto;
import com.capgemini.assessment.petclinic.integration.dto.OutboxStatsDto;
import com.capgemini.assessment.petclinic.integration.model.OutboxStatus;
import java.util.List;

/**
 * Application service interface for polling, relaying, monitoring, and replaying Transactional Outbox events.
 */
public interface OutboxRelayService {

    /**
     * Polls due PENDING and FAILED_RETRYABLE outbox events and attempts delivery via ExternalEventTransport.
     *
     * @return number of events successfully published during the batch run
     */
    int processDueEventsBatch();

    /**
     * Manually resets a failed or dead-lettered event by eventId and immediately attempts delivery.
     *
     * @param eventId unique event UUID to retry
     * @return updated OutboxEventResponseDto after the retry attempt
     */
    OutboxEventResponseDto retryEventById(String eventId);

    /**
     * Requeues all DEAD_LETTER events back to PENDING and triggers an immediate batch relay.
     *
     * @return number of dead-letter events requeued
     */
    int retryAllDeadLetterEvents();

    /**
     * Returns aggregated telemetry counts of outbox events across all lifecycle states.
     *
     * @return OutboxStatsDto summary
     */
    OutboxStatsDto getOutboxStats();

    /**
     * Lists outbox events filtered optionally by status.
     *
     * @param status optional OutboxStatus filter (null returns all events)
     * @return list of OutboxEventResponseDto records
     */
    List<OutboxEventResponseDto> listEvents(OutboxStatus status);

    /**
     * Retrieves a cursor-paginated feed of integration event envelopes for pull-based consumer catch-up.
     *
     * @param afterSequenceId last processed sequence ID (default 0)
     * @param limit maximum number of envelopes to return
     * @return ordered list of IntegrationEventEnvelope items
     */
    List<IntegrationEventEnvelope> fetchPullFeed(Long afterSequenceId, int limit);
}
