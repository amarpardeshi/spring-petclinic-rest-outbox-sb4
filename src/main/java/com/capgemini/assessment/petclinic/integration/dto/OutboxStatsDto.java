package com.capgemini.assessment.petclinic.integration.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Aggregated counts of Outbox events segmented by lifecycle status for operational dashboards.
 *
 * @param pendingCount number of events awaiting first delivery attempt
 * @param failedRetryableCount number of events scheduled for exponential backoff retry
 * @param publishedCount number of events successfully delivered
 * @param deadLetterCount number of events quarantined after exhausting max retries
 * @param totalCount total outbox events recorded
 */
@Schema(description = "Aggregated outbox telemetry counters by status")
public record OutboxStatsDto(
        long pendingCount,
        long failedRetryableCount,
        long publishedCount,
        long deadLetterCount,
        long totalCount
) {
}
