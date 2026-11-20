package com.capgemini.assessment.petclinic.integration;

import com.capgemini.assessment.petclinic.config.OutboxProperties;
import com.capgemini.assessment.petclinic.integration.dto.IntegrationEventEnvelope;
import com.capgemini.assessment.petclinic.integration.dto.OutboxEventResponseDto;
import com.capgemini.assessment.petclinic.integration.dto.TransportDeliveryResult;
import com.capgemini.assessment.petclinic.integration.model.AggregateType;
import com.capgemini.assessment.petclinic.integration.model.DomainEventType;
import com.capgemini.assessment.petclinic.integration.model.OutboxEvent;
import com.capgemini.assessment.petclinic.integration.model.OutboxStatus;
import com.capgemini.assessment.petclinic.integration.repository.OutboxEventRepository;
import com.capgemini.assessment.petclinic.integration.service.ExternalEventTransport;
import com.capgemini.assessment.petclinic.integration.service.OutboxRelayServiceImpl;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests verifying OutboxRelayServiceImpl state transitions, exponential backoff, dead-lettering, and manual replay.
 */
@ExtendWith(MockitoExtension.class)
class OutboxRelayServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ExternalEventTransport externalEventTransport;

    private OutboxRelayServiceImpl outboxRelayService;

    /**
     * Initializes OutboxRelayServiceImpl with a maxAttempts=3 configuration before each test case.
     */
    @BeforeEach
    void setUp() {
        OutboxProperties properties = new OutboxProperties(
                false,
                "http://localhost:9966/petclinic/api/v1/mock-external/webhook",
                10,
                3,
                2L,
                5000L,
                1000,
                1000
        );
        outboxRelayService = new OutboxRelayServiceImpl(
                outboxEventRepository,
                externalEventTransport,
                properties,
                new SimpleMeterRegistry()
        );
    }

    /**
     * Verifies that a PENDING event transitions to PUBLISHED when the external transport returns HTTP 200.
     */
    @Test
    void shouldMarkEventAsPublishedWhenTransportSucceeds() {
        OutboxEvent pendingEvent = new OutboxEvent(
                "evt-100",
                AggregateType.OWNER,
                "1",
                DomainEventType.OWNER_CREATED,
                "{"id":1,"firstName":"Amar"}",
                "hash-100",
                Instant.now().minusSeconds(5)
        );
        pendingEvent.setId(1L);

        Mockito.when(outboxEventRepository.findDueEvents(
                ArgumentMatchers.anyCollection(),
                ArgumentMatchers.any(Instant.class),
                ArgumentMatchers.any(Pageable.class)
        )).thenReturn(List.of(pendingEvent));

        Mockito.when(externalEventTransport.send(ArgumentMatchers.any(IntegrationEventEnvelope.class)))
                .thenReturn(TransportDeliveryResult.success(200, "OK"));

        int published = outboxRelayService.processDueEventsBatch();

        Assertions.assertEquals(1, published);
        Assertions.assertEquals(OutboxStatus.PUBLISHED, pendingEvent.getStatus());
        Assertions.assertEquals(1, pendingEvent.getAttemptCount());
        Assertions.assertNotNull(pendingEvent.getPublishedAt());
        Assertions.assertNull(pendingEvent.getLastError());
    }

    /**
     * Verifies that a transient failure increments attemptCount, sets status to FAILED_RETRYABLE, and advances nextAttemptAt.
     */
    @Test
    void shouldScheduleExponentialBackoffRetryOnTransientFailure() {
        Instant beforeAttempt = Instant.now();
        OutboxEvent pendingEvent = new OutboxEvent(
                "evt-200",
                AggregateType.PET,
                "5",
                DomainEventType.PET_UPDATED,
                "{"id":5,"name":"Leo"}",
                "hash-200",
                beforeAttempt.minusSeconds(5)
        );
        pendingEvent.setId(2L);

        Mockito.when(outboxEventRepository.findDueEvents(
                ArgumentMatchers.anyCollection(),
                ArgumentMatchers.any(Instant.class),
                ArgumentMatchers.any(Pageable.class)
        )).thenReturn(List.of(pendingEvent));

        Mockito.when(externalEventTransport.send(ArgumentMatchers.any(IntegrationEventEnvelope.class)))
                .thenReturn(TransportDeliveryResult.failure(503, "HTTP 503: Service Unavailable"));

        int published = outboxRelayService.processDueEventsBatch();

        Assertions.assertEquals(0, published);
        Assertions.assertEquals(OutboxStatus.FAILED_RETRYABLE, pendingEvent.getStatus());
        Assertions.assertEquals(1, pendingEvent.getAttemptCount());
        Assertions.assertTrue(pendingEvent.getNextAttemptAt().isAfter(beforeAttempt));
        Assertions.assertEquals("HTTP 503: Service Unavailable", pendingEvent.getLastError());
    }

    /**
     * Verifies that exhausting maxAttempts transitions the event to DEAD_LETTER and allows subsequent manual recovery.
     */
    @Test
    void shouldTransitionToDeadLetterAndAllowManualReplay() {
        OutboxEvent failingEvent = new OutboxEvent(
                "evt-300",
                AggregateType.VISIT,
                "9",
                DomainEventType.VISIT_CREATED,
                "{"id":9,"description":"Rabies shot"}",
                "hash-300",
                Instant.now().minusSeconds(10)
        );
        failingEvent.setId(3L);
        failingEvent.setStatus(OutboxStatus.FAILED_RETRYABLE);
        failingEvent.setAttemptCount(2);

        Mockito.when(outboxEventRepository.findDueEvents(
                ArgumentMatchers.anyCollection(),
                ArgumentMatchers.any(Instant.class),
                ArgumentMatchers.any(Pageable.class)
        )).thenReturn(List.of(failingEvent));

        Mockito.when(externalEventTransport.send(ArgumentMatchers.any(IntegrationEventEnvelope.class)))
                .thenReturn(TransportDeliveryResult.failure(503, "HTTP 503: Service Unavailable"));

        int published = outboxRelayService.processDueEventsBatch();

        Assertions.assertEquals(0, published);
        Assertions.assertEquals(OutboxStatus.DEAD_LETTER, failingEvent.getStatus());
        Assertions.assertEquals(3, failingEvent.getAttemptCount());

        Mockito.when(outboxEventRepository.findByEventId("evt-300"))
                .thenReturn(Optional.of(failingEvent));
        Mockito.when(externalEventTransport.send(ArgumentMatchers.any(IntegrationEventEnvelope.class)))
                .thenReturn(TransportDeliveryResult.success(200, "Recovered"));

        OutboxEventResponseDto replayed = outboxRelayService.retryEventById("evt-300");

        Assertions.assertEquals(OutboxStatus.PUBLISHED, replayed.status());
        Assertions.assertEquals(1, replayed.attemptCount());
        Assertions.assertNull(replayed.lastError());
    }
}
