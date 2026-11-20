package com.capgemini.assessment.petclinic.integration.health;

import com.capgemini.assessment.petclinic.integration.dto.OutboxStatsDto;
import com.capgemini.assessment.petclinic.integration.service.OutboxRelayService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Spring Boot Actuator HealthIndicator surfacing Outbox backlog and Dead-Letter metrics at /actuator/health.
 */
@Component("outboxIntegration")
public class OutboxHealthIndicator implements HealthIndicator {

    private final OutboxRelayService outboxRelayService;

    /**
     * Constructs OutboxHealthIndicator with OutboxRelayService dependency.
     *
     * @param outboxRelayService service providing outbox telemetry counters
     */
    public OutboxHealthIndicator(OutboxRelayService outboxRelayService) {
        this.outboxRelayService = outboxRelayService;
    }

    /**
     * Evaluates outbox health; reports UP with warning details or DOWN/OUT_OF_SERVICE if DEAD_LETTER events exist.
     *
     * @return Actuator Health status and telemetry attributes
     */
    @Override
    public Health health() {
        OutboxStatsDto stats = outboxRelayService.getOutboxStats();
        Health.Builder builder = (stats.deadLetterCount() > 0)
                ? Health.status("DEGRADED_DEAD_LETTER_PRESENT")
                : Health.up();
        return builder
                .withDetail("pendingEvents", stats.pendingCount())
                .withDetail("failedRetryableEvents", stats.failedRetryableCount())
                .withDetail("publishedEvents", stats.publishedCount())
                .withDetail("deadLetterEvents", stats.deadLetterCount())
                .withDetail("totalEvents", stats.totalCount())
                .build();
    }
}
