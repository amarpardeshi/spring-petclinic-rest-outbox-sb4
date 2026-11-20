package com.capgemini.assessment.petclinic.integration.service;

import com.capgemini.assessment.petclinic.integration.dto.IntegrationEventEnvelope;
import com.capgemini.assessment.petclinic.integration.dto.WebhookReceiptDto;
import com.capgemini.assessment.petclinic.integration.model.ExternalSimulationMode;
import com.capgemini.assessment.petclinic.integration.model.ProcessedWebhookEvent;
import com.capgemini.assessment.petclinic.integration.repository.ProcessedWebhookEventRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer-side idempotent inbox processor and failure simulator demonstrating duplicate suppression and chaos testing.
 */
@Service
public class IdempotentWebhookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdempotentWebhookService.class);

    private final ProcessedWebhookEventRepository processedWebhookEventRepository;
    private final AtomicReference<ExternalSimulationMode> simulationMode =
            new AtomicReference<>(ExternalSimulationMode.HEALTHY);

    /**
     * Constructs IdempotentWebhookService backed by ProcessedWebhookEventRepository.
     *
     * @param processedWebhookEventRepository repository storing deduplicated event receipts
     */
    public IdempotentWebhookService(ProcessedWebhookEventRepository processedWebhookEventRepository) {
        this.processedWebhookEventRepository = processedWebhookEventRepository;
    }

    /**
     * Processes an incoming IntegrationEventEnvelope idempotently, suppressing duplicates by eventId.
     *
     * @param envelope received integration event envelope
     * @param idempotencyHeader optional X-Idempotency-Key header value
     * @return WebhookReceiptDto indicating whether the event was newly processed or deduplicated
     */
    @Transactional
    public WebhookReceiptDto processIncomingWebhook(
            IntegrationEventEnvelope envelope,
            String idempotencyHeader) {
        String effectiveEventId = (idempotencyHeader != null && !idempotencyHeader.isBlank())
                ? idempotencyHeader
                : envelope.eventId();

        Optional<ProcessedWebhookEvent> existing = processedWebhookEventRepository.findByEventId(effectiveEventId);
        if (existing.isPresent()) {
            ProcessedWebhookEvent record = existing.get();
            LOGGER.info("Duplicate webhook detected and suppressed [eventId={}]", effectiveEventId);
            return new WebhookReceiptDto(
                    effectiveEventId,
                    true,
                    "DUPLICATE_IGNORED",
                    record.getReceivedAt()
            );
        }

        Instant now = Instant.now();
        ProcessedWebhookEvent saved = processedWebhookEventRepository.save(
                new ProcessedWebhookEvent(
                        effectiveEventId,
                        envelope.eventType(),
                        envelope.aggregateId(),
                        envelope.payloadHash(),
                        envelope.data(),
                        now
                )
        );
        LOGGER.info("Processed new webhook event [eventId={}, type={}]", effectiveEventId, envelope.eventType());
        return new WebhookReceiptDto(
                saved.getEventId(),
                false,
                "PROCESSED",
                saved.getReceivedAt()
        );
    }

    /**
     * Retrieves the current simulation mode of the mock external receiver.
     *
     * @return current ExternalSimulationMode
     */
    public ExternalSimulationMode getSimulationMode() {
        return simulationMode.get();
    }

    /**
     * Updates the simulation mode of the mock external receiver for failure testing.
     *
     * @param mode new ExternalSimulationMode to apply
     */
    public void setSimulationMode(ExternalSimulationMode mode) {
        this.simulationMode.set(mode);
        LOGGER.info("Updated mock external system simulation mode to {}", mode);
    }

    /**
     * Returns all deduplicated webhook events received by the mock external consumer.
     *
     * @return list of ProcessedWebhookEvent entities
     */
    @Transactional(readOnly = true)
    public List<ProcessedWebhookEvent> listProcessedEvents() {
        return processedWebhookEventRepository.findAll();
    }
}
