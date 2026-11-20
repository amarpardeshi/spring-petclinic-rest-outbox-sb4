package com.capgemini.assessment.petclinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed configuration properties binding for the PetClinic Transactional Outbox integration.
 *
 * @param enabled whether scheduled background outbox polling is enabled
 * @param webhookUrl target external webhook HTTP endpoint URL
 * @param batchSize maximum number of outbox events processed per polling cycle
 * @param maxAttempts maximum delivery attempts before quarantining an event to DEAD_LETTER
 * @param initialBackoffSeconds base exponential backoff delay in seconds
 * @param pollIntervalMs polling interval in milliseconds
 * @param connectTimeoutMs HTTP connection timeout in milliseconds
 * @param readTimeoutMs HTTP read timeout in milliseconds
 */
@ConfigurationProperties(prefix = "petclinic.integration.outbox")
public record OutboxProperties(
        boolean enabled,
        String webhookUrl,
        int batchSize,
        int maxAttempts,
        long initialBackoffSeconds,
        long pollIntervalMs,
        int connectTimeoutMs,
        int readTimeoutMs
) {
}
