package com.capgemini.assessment.petclinic.integration.service;

import com.capgemini.assessment.petclinic.common.constants.ApiConstants;
import com.capgemini.assessment.petclinic.config.OutboxProperties;
import com.capgemini.assessment.petclinic.integration.dto.IntegrationEventEnvelope;
import com.capgemini.assessment.petclinic.integration.dto.TransportDeliveryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Spring Boot 4 RestClient implementation of ExternalEventTransport that pushes events via HTTP webhooks.
 */
@Component
public class RestClientWebhookTransport implements ExternalEventTransport {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientWebhookTransport.class);

    private final RestClient outboxRestClient;
    private final OutboxProperties outboxProperties;

    /**
     * Constructs RestClientWebhookTransport with timeout-configured RestClient and outbox properties.
     *
     * @param outboxRestClient configured Spring RestClient instance
     * @param outboxProperties outbox configuration properties
     */
    public RestClientWebhookTransport(
            RestClient outboxRestClient,
            OutboxProperties outboxProperties) {
        this.outboxRestClient = outboxRestClient;
        this.outboxProperties = outboxProperties;
    }

    /**
     * Dispatches the integration event envelope as an HTTP POST request with idempotency and checksum headers.
     *
     * @param envelope CloudEvents-aligned event envelope
     * @return TransportDeliveryResult indicating HTTP status and delivery outcome
     */
    @Override
    public TransportDeliveryResult send(IntegrationEventEnvelope envelope) {
        try {
            ResponseEntity<String> response = outboxRestClient.post()
                    .uri(outboxProperties.webhookUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(ApiConstants.HEADER_IDEMPOTENCY_KEY, envelope.eventId())
                    .header(ApiConstants.HEADER_EVENT_TYPE, envelope.eventType())
                    .header(ApiConstants.HEADER_PAYLOAD_HASH, envelope.payloadHash())
                    .body(envelope)
                    .retrieve()
                    .toEntity(String.class);

            int statusCode = response.getStatusCode().value();
            if (response.getStatusCode().is2xxSuccessful()) {
                return TransportDeliveryResult.success(statusCode, "Delivered successfully");
            }
            return TransportDeliveryResult.failure(statusCode, "Unexpected HTTP status: " + statusCode);
        } catch (RestClientResponseException exception) {
            int statusCode = exception.getStatusCode().value();
            String errorMsg = "HTTP " + statusCode + ": " + exception.getStatusText();
            LOGGER.warn("Webhook delivery failed for eventId={} with {}", envelope.eventId(), errorMsg);
            return TransportDeliveryResult.failure(statusCode, errorMsg);
        } catch (Exception exception) {
            String errorMsg = "Transport error: " + exception.getMessage();
            LOGGER.warn("Webhook network/timeout failure for eventId={}: {}", envelope.eventId(), errorMsg);
            return TransportDeliveryResult.failure(0, errorMsg);
        }
    }
}
