package com.capgemini.assessment.petclinic.integration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Acknowledgement response returned by the external webhook receiver confirming idempotent handling.
 *
 * @param eventId unique event UUID processed
 * @param duplicate true if the event had already been processed previously
 * @param status processing outcome description
 * @param processedAt UTC timestamp when the event was recorded
 */
@Schema(description = "Webhook consumer idempotent acknowledgement receipt")
public record WebhookReceiptDto(
        String eventId,
        boolean duplicate,
        String status,
        Instant processedAt
) {
}
