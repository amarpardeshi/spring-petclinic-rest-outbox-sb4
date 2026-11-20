package com.capgemini.assessment.petclinic.integration;

import com.capgemini.assessment.petclinic.integration.dto.IntegrationEventEnvelope;
import com.capgemini.assessment.petclinic.integration.dto.WebhookReceiptDto;
import com.capgemini.assessment.petclinic.integration.model.ProcessedWebhookEvent;
import com.capgemini.assessment.petclinic.integration.repository.ProcessedWebhookEventRepository;
import com.capgemini.assessment.petclinic.integration.service.IdempotentWebhookService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests verifying consumer-side duplicate event detection and idempotent acknowledgement.
 */
@ExtendWith(MockitoExtension.class)
class IdempotentWebhookServiceTest {

    @Mock
    private ProcessedWebhookEventRepository processedWebhookEventRepository;

    private IdempotentWebhookService idempotentWebhookService;

    /**
     * Initializes IdempotentWebhookService before each test execution.
     */
    @BeforeEach
    void setUp() {
        idempotentWebhookService = new IdempotentWebhookService(processedWebhookEventRepository);
    }

    /**
     * Verifies that a first-seen eventId is persisted and marked with duplicate=false.
     */
    @Test
    void shouldProcessNewEventWhenNotInInbox() {
        IntegrationEventEnvelope envelope = new IntegrationEventEnvelope(
                "1.0",
                "evt-unique-01",
                10L,
                "OWNER_CREATED",
                "OWNER",
                "1",
                Instant.now(),
                "sha256-abc",
                1,
                "{"id":1}"
        );

        Mockito.when(processedWebhookEventRepository.findByEventId("evt-unique-01"))
                .thenReturn(Optional.empty());
        Mockito.when(processedWebhookEventRepository.save(ArgumentMatchers.any(ProcessedWebhookEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WebhookReceiptDto receipt = idempotentWebhookService.processIncomingWebhook(envelope, "evt-unique-01");

        Assertions.assertFalse(receipt.duplicate());
        Assertions.assertEquals("PROCESSED", receipt.status());
        Assertions.assertEquals("evt-unique-01", receipt.eventId());
    }

    /**
     * Verifies that redelivery of an already-processed eventId returns duplicate=true without saving a duplicate row.
     */
    @Test
    void shouldSuppressDuplicateEventWhenAlreadyProcessed() {
        Instant originalTime = Instant.now().minusSeconds(60);
        ProcessedWebhookEvent existingRecord = new ProcessedWebhookEvent(
                "evt-dup-02",
                "OWNER_UPDATED",
                "1",
                "sha256-xyz",
                "{"id":1}",
                originalTime
        );
        IntegrationEventEnvelope envelope = new IntegrationEventEnvelope(
                "1.0",
                "evt-dup-02",
                11L,
                "OWNER_UPDATED",
                "OWNER",
                "1",
                Instant.now(),
                "sha256-xyz",
                2,
                "{"id":1}"
        );

        Mockito.when(processedWebhookEventRepository.findByEventId("evt-dup-02"))
                .thenReturn(Optional.of(existingRecord));

        WebhookReceiptDto receipt = idempotentWebhookService.processIncomingWebhook(envelope, "evt-dup-02");

        Assertions.assertTrue(receipt.duplicate());
        Assertions.assertEquals("DUPLICATE_IGNORED", receipt.status());
        Assertions.assertEquals(originalTime, receipt.processedAt());
        Mockito.verify(processedWebhookEventRepository, Mockito.never())
                .save(ArgumentMatchers.any(ProcessedWebhookEvent.class));
    }
}
