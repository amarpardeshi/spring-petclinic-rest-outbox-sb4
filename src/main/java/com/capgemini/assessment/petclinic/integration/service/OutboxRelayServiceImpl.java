package com.capgemini.assessment.petclinic.integration.service;

import com.capgemini.assessment.petclinic.common.constants.ApiConstants;
import com.capgemini.assessment.petclinic.common.exception.ResourceNotFoundException;
import com.capgemini.assessment.petclinic.config.OutboxProperties;
import com.capgemini.assessment.petclinic.integration.dto.IntegrationEventEnvelope;
import com.capgemini.assessment.petclinic.integration.dto.OutboxEventResponseDto;
import com.capgemini.assessment.petclinic.integration.dto.OutboxStatsDto;
import com.capgemini.assessment.petclinic.integration.dto.TransportDeliveryResult;
import com.capgemini.assessment.petclinic.integration.model.OutboxEvent;
import com.capgemini.assessment.petclinic.integration.model.OutboxStatus;
import com.capgemini.assessment.petclinic.integration.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of OutboxRelayService managing exponential backoff retries, dead-lettering, and metrics.
 */
@Service
public class OutboxRelayServiceImpl implements OutboxRelayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxRelayServiceImpl.class);
    private static final Set<OutboxStatus> DUE_STATUSES = EnumSet.of(
            OutboxStatus.PENDING,
            OutboxStatus.FAILED_RETRYABLE
    );

    private final OutboxEventRepository outboxEventRepository;
    private final ExternalEventTransport externalEventTransport;
    private final OutboxProperties outboxProperties;
    private final Counter publishedCounter;
    private final Counter retryCounter;
    private final Counter deadLetterCounter;

    /**
     * Constructs OutboxRelayServiceImpl with repository, transport adapter, configuration, and Micrometer registry.
     *
     * @param outboxEventRepository repository for outbox persistence
     * @param externalEventTransport outbound webhook transport
     * @param outboxProperties retry and batch configuration properties
     * @param meterRegistry Micrometer meter registry for observability metrics
     */
    public OutboxRelayServiceImpl(
            OutboxEventRepository outboxEventRepository,
            ExternalEventTransport externalEventTransport,
            OutboxProperties outboxProperties,
            MeterRegistry meterRegistry) {
        this.outboxEventRepository = outboxEventRepository;
        this.externalEventTransport = externalEventTransport;
        this.outboxProperties = outboxProperties;
        this.publishedCounter = Counter.builder("petclinic.outbox.published")
                .description("Total number of outbox events successfully delivered")
                .register(meterRegistry);
        this.retryCounter = Counter.builder("petclinic.outbox.retried")
                .description("Total number of transient outbox delivery failures scheduled for retry")
                .register(meterRegistry);
        this.deadLetterCounter = Counter.builder("petclinic.outbox.dead_letter")
                .description("Total number of outbox events quarantined to DEAD_LETTER")
                .register(meterRegistry);
    }

    /**
     * Polls a bounded batch of due outbox events and dispatches each event through the transport layer.
     *
     * @return count of events that transitioned to PUBLISHED
     */
    @Override
    @Transactional
    public int processDueEventsBatch() {
        List<OutboxEvent> dueEvents = outboxEventRepository.findDueEvents(
                DUE_STATUSES,
                Instant.now(),
                PageRequest.of(0, outboxProperties.batchSize())
        );
        int publishedCount = 0;
        for (OutboxEvent event : dueEvents) {
            if (attemptSingleDelivery(event)) {
                publishedCount++;
            }
        }
        return publishedCount;
    }

    /**
     * Resets a specific outbox event by its eventId and triggers an immediate delivery attempt.
     *
     * @param eventId unique UUID of the outbox event
     * @return updated OutboxEventResponseDto reflecting the retry result
     */
    @Override
    @Transactional
    public OutboxEventResponseDto retryEventById(String eventId) {
        OutboxEvent event = outboxEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("OutboxEvent", eventId));
        if (event.getStatus() == OutboxStatus.DEAD_LETTER) {
            event.setAttemptCount(0);
        }
        event.setStatus(OutboxStatus.PENDING);
        event.setNextAttemptAt(Instant.now());
        attemptSingleDelivery(event);
        return toResponseDto(event);
    }

    /**
     * Requeues all quarantined DEAD_LETTER events back to PENDING with reset attempt counts and processes due events.
     *
     * @return number of DEAD_LETTER events requeued
     */
    @Override
    @Transactional
    public int retryAllDeadLetterEvents() {
        List<OutboxEvent> deadLetterEvents = outboxEventRepository.findByStatusOrderByIdAsc(OutboxStatus.DEAD_LETTER);
        Instant now = Instant.now();
        for (OutboxEvent event : deadLetterEvents) {
            event.setStatus(OutboxStatus.PENDING);
            event.setAttemptCount(0);
            event.setNextAttemptAt(now);
            event.setLastError(null);
        }
        outboxEventRepository.saveAll(deadLetterEvents);
        processDueEventsBatch();
        return deadLetterEvents.size();
    }

    /**
     * Computes aggregated counts of outbox events segmented by status.
     *
     * @return OutboxStatsDto telemetry snapshot
     */
    @Override
    @Transactional(readOnly = true)
    public OutboxStatsDto getOutboxStats() {
        long pending = outboxEventRepository.countByStatus(OutboxStatus.PENDING);
        long retryable = outboxEventRepository.countByStatus(OutboxStatus.FAILED_RETRYABLE);
        long published = outboxEventRepository.countByStatus(OutboxStatus.PUBLISHED);
        long deadLetter = outboxEventRepository.countByStatus(OutboxStatus.DEAD_LETTER);
        long total = pending + retryable + published + deadLetter;
        return new OutboxStatsDto(pending, retryable, published, deadLetter, total);
    }

    /**
     * Lists outbox events filtered optionally by status.
     *
     * @param status optional status filter
     * @return list of OutboxEventResponseDto objects
     */
    @Override
    @Transactional(readOnly = true)
    public List<OutboxEventResponseDto> listEvents(OutboxStatus status) {
        List<OutboxEvent> events = (status == null)
                ? outboxEventRepository.findAll()
                : outboxEventRepository.findByStatusOrderByIdAsc(status);
        return events.stream().map(this::toResponseDto).toList();
    }

    /**
     * Returns cursor-paginated integration event envelopes with sequence ID greater than afterSequenceId.
     *
     * @param afterSequenceId cursor sequence ID
     * @param limit maximum items to return
     * @return list of IntegrationEventEnvelope records
     */
    @Override
    @Transactional(readOnly = true)
    public List<IntegrationEventEnvelope> fetchPullFeed(Long afterSequenceId, int limit) {
        long safeCursor = (afterSequenceId == null || afterSequenceId < 0) ? 0L : afterSequenceId;
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<OutboxEvent> events = outboxEventRepository.findByIdGreaterThanOrderByIdAsc(
                safeCursor,
                PageRequest.of(0, safeLimit)
        );
        return events.stream().map(this::toEnvelope).toList();
    }

    /**
     * Executes a single delivery attempt for an OutboxEvent and updates its state machine attributes.
     *
     * @param event OutboxEvent to deliver
     * @return true if delivery succeeded, false otherwise
     */
    private boolean attemptSingleDelivery(OutboxEvent event) {
        int currentAttempt = event.getAttemptCount() + 1;
        event.setAttemptCount(currentAttempt);
        IntegrationEventEnvelope envelope = toEnvelope(event);
        TransportDeliveryResult result = externalEventTransport.send(envelope);

        if (result.delivered()) {
            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
            event.setLastError(null);
            outboxEventRepository.save(event);
            publishedCounter.increment();
            LOGGER.info("Outbox event published [eventId={}, attempt={}]", event.getEventId(), currentAttempt);
            return true;
        }

        event.setLastError(result.diagnosticMessage());
        if (currentAttempt >= outboxProperties.maxAttempts()) {
            event.setStatus(OutboxStatus.DEAD_LETTER);
            outboxEventRepository.save(event);
            deadLetterCounter.increment();
            LOGGER.error("Outbox event moved to DEAD_LETTER after {} attempts [eventId={}, reason={}]",
                    currentAttempt, event.getEventId(), result.diagnosticMessage());
        } else {
            long backoffSeconds = calculateExponentialBackoffSeconds(currentAttempt);
            event.setStatus(OutboxStatus.FAILED_RETRYABLE);
            event.setNextAttemptAt(Instant.now().plusSeconds(backoffSeconds));
            outboxEventRepository.save(event);
            retryCounter.increment();
            LOGGER.warn("Outbox event scheduled for retry [eventId={}, attempt={}, nextInSec={}]",
                    event.getEventId(), currentAttempt, backoffSeconds);
        }
        return false;
    }

    /**
     * Calculates exponential backoff delay in seconds using baseDelay * 2^(attempt - 1).
     *
     * @param attemptCount 1-based attempt number
     * @return backoff delay in seconds
     */
    private long calculateExponentialBackoffSeconds(int attemptCount) {
        long multiplier = 1L << Math.min(attemptCount - 1, 10);
        return Math.max(1L, outboxProperties.initialBackoffSeconds()) * multiplier;
    }

    /**
     * Maps an OutboxEvent entity to a wire-level IntegrationEventEnvelope.
     *
     * @param event persistent OutboxEvent entity
     * @return IntegrationEventEnvelope instance
     */
    private IntegrationEventEnvelope toEnvelope(OutboxEvent event) {
        Instant occurredAt = (event.getCreatedAt() != null) ? event.getCreatedAt() : Instant.now();
        return new IntegrationEventEnvelope(
                ApiConstants.SPEC_VERSION,
                event.getEventId(),
                event.getId(),
                event.getEventType().name(),
                event.getAggregateType().name(),
                event.getAggregateId(),
                occurredAt,
                event.getPayloadHash(),
                event.getAttemptCount(),
                event.getPayloadJson()
        );
    }

    /**
     * Maps an OutboxEvent entity to an operational OutboxEventResponseDto.
     *
     * @param event persistent OutboxEvent entity
     * @return OutboxEventResponseDto instance
     */
    private OutboxEventResponseDto toResponseDto(OutboxEvent event) {
        return new OutboxEventResponseDto(
                event.getId(),
                event.getEventId(),
                event.getAggregateType(),
                event.getAggregateId(),
                event.getEventType(),
                event.getStatus(),
                event.getAttemptCount(),
                event.getNextAttemptAt(),
                event.getPublishedAt(),
                event.getLastError(),
                event.getPayloadHash(),
                event.getPayloadJson(),
                event.getCreatedAt()
        );
    }
}
