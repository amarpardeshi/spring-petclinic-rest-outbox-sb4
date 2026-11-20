package com.capgemini.assessment.petclinic.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configures the Spring Framework 7 / Spring Boot 4 RestClient used for outbound webhook delivery.
 */
@Configuration
public class HttpClientConfig {

    /**
     * Default constructor for HttpClientConfig.
     */
    public HttpClientConfig() {
        // Default constructor
    }

    /**
     * Creates a RestClient configured with explicit connection and read timeouts so external slowdowns never hang threads.
     *
     * @param properties outbox integration configuration properties
     * @return timeout-hardened RestClient bean
     */
    @Bean
    public RestClient outboxRestClient(OutboxProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.connectTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.readTimeoutMs()));
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
