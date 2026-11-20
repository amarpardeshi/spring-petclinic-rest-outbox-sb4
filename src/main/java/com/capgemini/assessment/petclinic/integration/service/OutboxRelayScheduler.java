package com.capgemini.assessment.petclinic.integration.service;

import com.capgemini.assessment.petclinic.config.OutboxProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled background worker that periodically triggers OutboxRelayService when outbox polling is enabled.
 */
@Component
public class OutboxRelayScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxRelayScheduler.class);

    private final OutboxRelayService outboxRelayService;
    private final OutboxProperties outboxProperties;

    /**
     * Constructs OutboxRelayScheduler with the relay service and configuration properties.
     *
     * @param outboxRelayService outbox relay orchestration service
     * @param outboxProperties configuration properties controlling polling state
     */
    public OutboxRelayScheduler(
            OutboxRelayService outboxRelayService,
            OutboxProperties outboxProperties) {
        this.outboxRelayService = outboxRelayService;
        this.outboxProperties = outboxProperties;
    }

    /**
     * Executes scheduled polling of due outbox events at the configured fixed delay interval.
     */
    @Scheduled(fixedDelayString = "${petclinic.integration.outbox.poll-interval-ms:5000}")
    public void pollAndRelayOutbox() {
        if (!outboxProperties.enabled()) {
            return;
        }
        int delivered = outboxRelayService.processDueEventsBatch();
        if (delivered > 0) {
            LOGGER.debug("Scheduled outbox relay published {} event(s)", delivered);
        }
    }
}
